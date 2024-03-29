package rocks.shumyk.hadoop.ai.language.detector.model;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import rocks.shumyk.hadoop.ai.language.detector.model.value.Bigram;
import rocks.shumyk.hadoop.ai.language.detector.model.value.Probability;

public class BigramsProbabilities extends HashMap<Bigram, LanguageProbabilities> {
  public BigramsProbabilities(final int initialCapacity) {
    super(initialCapacity);
  }

  public void add(final LanguageProfile profile,
                  final BigramOccurrences bigram) {
    final Probability probability = bigram.probability(profile.totalOccurrences());
    this.compute(
        bigram.name(),
        (i, old) -> ofNullable(old)
            .orElseGet(LanguageProbabilities::new)
            .add(profile.language(), probability)
    );
  }
}
