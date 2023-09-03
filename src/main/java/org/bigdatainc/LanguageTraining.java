package org.bigdatainc;

import java.io.IOException;
import java.util.stream.StreamSupport;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.bigdatainc.model.value.Bigram;
import org.bigdatainc.util.Texts;

public class LanguageTraining {
  private LanguageTraining() {}

  public static final int MIN_OCCURRENCES = 7;

  public static class TrainingMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private static final IntWritable one = new IntWritable(1);

    @Override
    protected void map(final LongWritable key,
                       final Text value,
                       final Context context) throws IOException, InterruptedException {
      for (Bigram bigram : BigramExtractor.extractCounted(value.toString()).keySet())
        context.write(Texts.quotes(bigram.name()), one);
    }
  }

  public static class TraningReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    protected void reduce(final Text key,
                          final Iterable<IntWritable> values,
                          final Context context) throws IOException, InterruptedException {
      final int sum = StreamSupport.stream(values.spliterator(), false)
          .mapToInt(IntWritable::get)
          .sum();
      if (sum > LanguageTraining.MIN_OCCURRENCES)
        context.write(key, new IntWritable(sum));
    }
  }
}