package rocks.shumyk.hadoop.ai.language.detector;

import static java.util.Objects.nonNull;
import static org.apache.hadoop.thirdparty.com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import rocks.shumyk.hadoop.ai.language.detector.util.FileUtil;

public class HadoopJobRunner {
  @SuppressWarnings("java:S107")
  private HadoopJobRunner(final String jobName,
                          final Set<Entry<String, String>> configs,
                          final Class<? extends Mapper<?, ?, ?, ?>> mapperClass,
                          final Class<? extends Reducer<?, ?, ?, ?>> reducerClass,
                          final Class<?> mapOutputKeyClass,
                          final Class<?> mapOutputValueClass,
                          final Class<?> outputKeyClass,
                          final Class<?> outputValueClass,
                          final String inputPath,
                          final String outputPath,
                          final Runnable cleanup) {
    this.jobName = jobName;
    this.configs = configs;
    this.mapperClass = mapperClass;
    this.reducerClass = reducerClass;
    this.mapOutputKeyClass = nonNull(mapOutputKeyClass) ? mapOutputKeyClass : outputKeyClass;
    this.mapOutputValueClass = nonNull(mapOutputValueClass) ? mapOutputValueClass : outputValueClass;
    this.outputKeyClass = outputKeyClass;
    this.outputValueClass = outputValueClass;
    this.inputPath = inputPath;
    this.outputPath = outputPath;
    this.cleanup = cleanup;
  }

  private final String jobName;
  private final Set<Entry<String, String>> configs;
  private final Class<? extends Mapper<?, ?, ?, ?>> mapperClass;
  private final Class<? extends Reducer<?, ?, ?, ?>> reducerClass;
  private final Class<?> mapOutputKeyClass;
  private final Class<?> mapOutputValueClass;
  private final Class<?> outputKeyClass;
  private final Class<?> outputValueClass;
  private final String inputPath;
  private final String outputPath;
  private final Runnable cleanup;

  public static Builder builder(final String jobName) {
    return new Builder(jobName);
  }

  public void run() throws IOException, InterruptedException, ClassNotFoundException {
    final Configuration config = new Configuration();
    configs.forEach(entry -> config.set(entry.getKey(), entry.getValue()));

    final Job job = Job.getInstance(config, jobName);
    job.setJarByClass(mapperClass);
    job.setMapperClass(mapperClass);
    job.setReducerClass(reducerClass);

    job.setMapOutputKeyClass(mapOutputKeyClass);
    job.setMapOutputValueClass(mapOutputValueClass);
    job.setOutputKeyClass(outputKeyClass);
    job.setOutputValueClass(outputValueClass);

    FileInputFormat.addInputPath(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    FileUtil.preClean(outputPath);
    if (job.waitForCompletion(false))
      cleanup.run();
  }


  public static class Builder {
    private Builder(final String jobName) {
      this.jobName = jobName;
      this.configs = new HashSet<>();
    }

    private final String jobName;
    private final Set<Entry<String, String>> configs;
    private Class<? extends Mapper<?, ?, ?, ?>> mapperClass;
    private Class<? extends Reducer<?, ?, ?, ?>> reducerClass;
    private Class<?> mapOutputKeyClass;
    private Class<?> mapOutputValueClass;
    private Class<?> outputKeyClass;
    private Class<?> outputValueClass;
    private String inputPath;
    private String outputPath;
    private Runnable cleanup;

    public Builder config(final String key, final String value) {
      this.configs.add(Map.entry(key, value));
      return this;
    }

    public Builder mapper(final Class<? extends Mapper<?, ?, ?, ?>> mapperClass) {
      this.mapperClass = mapperClass;
      return this;
    }

    public Builder reducer(final Class<? extends Reducer<?, ?, ?, ?>> reducerClass) {
      this.reducerClass = reducerClass;
      return this;
    }

    public Builder mapOutputKey(final Class<?> mapOutputKeyClass) {
      this.mapOutputKeyClass = mapOutputKeyClass;
      return this;
    }

    public Builder mapOutputValue(final Class<?> mapOutputValueClass) {
      this.mapOutputValueClass = mapOutputValueClass;
      return this;
    }

    public Builder outputKey(final Class<?> outputKeyClass) {
      this.outputKeyClass = outputKeyClass;
      return this;
    }

    public Builder outputValue(final Class<?> outputValueClass) {
      this.outputValueClass = outputValueClass;
      return this;
    }

    public Builder inputPath(final String inputPath) {
      this.inputPath = inputPath;
      return this;
    }

    public Builder outputPath(final String outputPath) {
      this.outputPath = outputPath;
      return this;
    }

    public Builder cleanup(final Runnable cleanup) {
      this.cleanup = cleanup;
      return this;
    }

    public HadoopJobRunner build() {
      validateArguments();
      return new HadoopJobRunner(
          jobName,
          configs,
          mapperClass,
          reducerClass,
          mapOutputKeyClass,
          mapOutputValueClass,
          outputKeyClass,
          outputValueClass,
          inputPath,
          outputPath,
          cleanup
      );
    }

    private void validateArguments() {
      checkArgument(nonNull(jobName), "Job name is null!");
      checkArgument(nonNull(mapperClass), "Mapper class is null!");
      checkArgument(nonNull(reducerClass), "Reducer class is null!");
      checkArgument(nonNull(outputKeyClass), "Output key class is null!");
      checkArgument(nonNull(outputValueClass), "Output value class is null!");
      checkArgument(nonNull(inputPath), "Input path is null!");
      checkArgument(nonNull(outputPath), "Output path is null!");
      checkArgument(nonNull(cleanup), "Cleanup is null!");
    }
  }
}
