package edu.cmu.cs.ziy.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class PhraseCollectionMentionChecker {

  private Map<Coordinate, String> phraseCoordinateToToken = Maps.newHashMap();

  private SetMultimap<String, Coordinate> phraseInitialTokenToCoordinate = HashMultimap.create();

  private HashSet<String> phraseInitialTokens = Sets.newHashSet();

  private List<String> phrases = Lists.newArrayList();

  private Splitter splitter;

  public PhraseCollectionMentionChecker(Set<String> phraseStrings, Splitter splitter) {
    int docIndex = 0;
    for (String phraseString : phraseStrings) {
      String[] tokens = Iterables.toArray(splitter.split(phraseString), String.class);
      for (int tokenPosition = 0; tokenPosition < tokens.length; tokenPosition++) {
        Coordinate coordinate = new Coordinate(docIndex, tokenPosition, tokens.length);
        this.phraseCoordinateToToken.put(coordinate, tokens[tokenPosition]);
      }
      this.phraseInitialTokenToCoordinate
              .put(tokens[0], new Coordinate(docIndex, 0, tokens.length));
      this.phraseInitialTokens.add(tokens[0]);
      this.phrases.add(phraseString);
      docIndex++;
    }
    this.splitter = splitter;
  }

  public Set<String> checkDocument(String text) {
    // Inverted index for document text
    String[] tokens = Iterables.toArray(splitter.split(text), String.class);
    SetMultimap<String, Coordinate> documentTokenToCoordinate = HashMultimap.create();
    Map<Coordinate, String> documentCoordinateToToken = Maps.newHashMap();
    Set<String> documentTokens = Sets.newHashSet();
    for (int tokenPosition = 0; tokenPosition < tokens.length; tokenPosition++) {
      Coordinate coordinate = new Coordinate(-1, tokenPosition, tokens.length);
      documentTokenToCoordinate.put(tokens[tokenPosition], coordinate);
      documentCoordinateToToken.put(coordinate, tokens[tokenPosition]);
      documentTokens.add(tokens[tokenPosition]);
    }
    // Search
    // To reduce replicated calculation, tokens with coordinate > 0 will be checked immediately
    // inside the loop after this.
    Set<String> sharedInitialTokens = Sets.intersection(documentTokens, phraseInitialTokens);
    Set<Coordinate> completedPhrases = Sets.newHashSet();
    next: for (String sharedToken : sharedInitialTokens) {
      Set<Coordinate> currPhraseCoordinates = phraseInitialTokenToCoordinate.get(sharedToken);
      Set<Coordinate> currDocumentCoordinates = documentTokenToCoordinate.get(sharedToken);
      do {
        // advance phrase tokens
        Set<Coordinate> currCompletedPhrases = Sets.filter(currPhraseCoordinates,
                new Coordinate.TokenEndOfPhrasePredicate());
        if (currCompletedPhrases != null && !currCompletedPhrases.isEmpty()) {
          completedPhrases.addAll(currCompletedPhrases);
        }
        Set<Coordinate> nextPhraseCoordinates = Sets.newHashSet(Collections2.transform(
                Sets.difference(currPhraseCoordinates, currCompletedPhrases),
                new Coordinate.AdvanceTokenIndexFunction()));
        if (nextPhraseCoordinates == null || nextPhraseCoordinates.isEmpty()) {
          // all phrases are processed, i.e. max(len(phases)) iterations have reached
          continue next;
        }
        // advance document tokens
        Set<Coordinate> nextDocumentCoordinates = Sets.newHashSet(FluentIterable
                .from(currDocumentCoordinates)
                .filter(Predicates.not(new Coordinate.TokenEndOfPhrasePredicate()))
                .transform(new Coordinate.AdvanceTokenIndexFunction()));
        if (nextDocumentCoordinates == null || nextDocumentCoordinates.isEmpty()) {
          // document doesn't contain some subsequent tokens of phrases with already matched prefix
          continue next;
        }
        // filter document collections
        Set<String> nextPhraseTokens = Sets.newHashSet(getMapValues(phraseCoordinateToToken,
                nextPhraseCoordinates));
        Set<String> nextDocumentTokens = Sets.newHashSet(getMapValues(documentCoordinateToToken,
                nextDocumentCoordinates));
        Set<String> nextSharedTokens = Sets.intersection(nextPhraseTokens, nextDocumentTokens);
        if (nextSharedTokens == null || nextSharedTokens.isEmpty()) {
          // no shared phrase prefix exists
          continue next;
        }
        currPhraseCoordinates = Sets.filter(nextPhraseCoordinates,
                new Coordinate.TokenIsContainedIn(phraseCoordinateToToken, nextSharedTokens));
        currDocumentCoordinates = Sets.filter(nextDocumentCoordinates,
                new Coordinate.TokenIsContainedIn(documentCoordinateToToken, nextSharedTokens));
      } while (true);
    }
    return Sets.newHashSet(Collections2.transform(completedPhrases,
            new Coordinate.CoordinateToPhraseFunction(phrases)));
  }

  private static <K, V> Collection<V> getMapValues(Map<K, V> map, Collection<K> keys) {
    Collection<V> values = Lists.newArrayList();
    for (K key : keys) {
      values.add(map.get(key));
    }
    return values;
  }

  public static void main(String[] args) {
    Set<String> phraseStrings = Sets.newHashSet(new String[] { "a", "a.b.c", "c", "a a" });
    Splitter splitter = Splitter.on(CharMatcher.anyOf("\" ();,.'[]{}!?:”“…\n\r\t]")).trimResults()
            .omitEmptyStrings();
    PhraseCollectionMentionChecker checker = new PhraseCollectionMentionChecker(phraseStrings,
            splitter);
    Set<String> results = checker.checkDocument("b c");
    System.out.println(results);
  }
}

class Coordinate {

  public static class CoordinateToPhraseFunction implements Function<Coordinate, String> {

    private List<String> phrases;

    public CoordinateToPhraseFunction(List<String> phrases) {
      this.phrases = phrases;
    }

    @Override
    public String apply(Coordinate input) {
      return phrases.get(input.docIndex);
    }

  }

  public static class TokenIsContainedIn implements Predicate<Coordinate> {

    private Map<Coordinate, String> coordinateToToken;

    private Set<String> tokens;

    public TokenIsContainedIn(Map<Coordinate, String> coordinateToToken, Set<String> tokens) {
      this.coordinateToToken = coordinateToToken;
      this.tokens = tokens;
    }

    @Override
    public boolean apply(Coordinate input) {
      return tokens.contains(coordinateToToken.get(input));
    }

  }

  public static class AdvanceTokenIndexFunction implements Function<Coordinate, Coordinate> {

    @Override
    public Coordinate apply(Coordinate input) {
      return new Coordinate(input.docIndex, input.tokenPosition + 1, input.docLength);
    }

  }

  public static class CoordinateDifferentPredicate implements Predicate<Coordinate> {

    private int tokenPositionDiff;

    private Collection<Coordinate> followingCoordinates;

    public CoordinateDifferentPredicate(int tokenPositionDiff,
            Collection<Coordinate> followingCoordinates) {
      this.tokenPositionDiff = tokenPositionDiff;
      this.followingCoordinates = followingCoordinates;
    }

    @Override
    public boolean apply(Coordinate baseCoordinate) {
      return followingCoordinates.contains(new Coordinate(baseCoordinate.docIndex,
              baseCoordinate.tokenPosition + tokenPositionDiff, baseCoordinate.docLength));
    }
  }

  public static class TokenEndOfPhrasePredicate implements Predicate<Coordinate> {

    @Override
    public boolean apply(Coordinate input) {
      return input.tokenPosition == input.docLength - 1;
    }

  }

  private int docIndex;

  private int tokenPosition;

  private int docLength;

  public Coordinate(int docIndex, int tokenPosition, int docLength) {
    super();
    this.docIndex = docIndex;
    this.tokenPosition = tokenPosition;
    this.docLength = docLength;
    assert tokenPosition < docLength;
  }

  public int getDocIndex() {
    return docIndex;
  }

  public int getTokenPosition() {
    return tokenPosition;
  }

  public int getDocLength() {
    return docLength;
  }

  @Override
  public String toString() {
    return "(" + docIndex + ", " + tokenPosition + "/" + docLength + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + docIndex;
    result = prime * result + tokenPosition;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Coordinate other = (Coordinate) obj;
    if (docIndex != other.docIndex)
      return false;
    if (tokenPosition != other.tokenPosition)
      return false;
    return true;
  }

}