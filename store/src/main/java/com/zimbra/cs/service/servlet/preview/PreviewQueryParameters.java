package com.zimbra.cs.service.servlet.preview;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zextras.carbonio.preview.queries.Query;
import java.util.Optional;
import javax.swing.text.html.Option;

/**
 * {@link PreviewQueryParameters} class is used to map the url parameter as java object and form a
 * final {@link Query} object for preview service
 */
public class PreviewQueryParameters {

  @JsonProperty("quality")
  private PreviewQueryParameters.Quality quality;

  @JsonProperty("output_format")
  private PreviewQueryParameters.Format outputFormat;

  @JsonProperty("crop")
  private Boolean crop;

  @JsonProperty("shape")
  private PreviewQueryParameters.Shape shape;

  @JsonProperty("first_page")
  private Integer firstPage;

  @JsonProperty("last_page")
  private Integer lastPage;

  @JsonProperty("lang_tag")
  private String langTag;

  public PreviewQueryParameters(
      PreviewQueryParameters.Quality quality, PreviewQueryParameters.Format outputFormat, PreviewQueryParameters.Shape shape) {
    this.quality = quality;
    this.outputFormat = outputFormat;
    this.shape = shape;
  }

  @SuppressWarnings("unused") // unused but required for testing
  public PreviewQueryParameters() {}

  public Optional<String> getQuality() {
    return Optional.ofNullable(quality == null ? null : quality.name());
  }

  public Optional<String> getOutputFormat() {
    return Optional.ofNullable(outputFormat == null ? null : outputFormat.name());
  }

  public Optional<Boolean> getCrop() {
    return Optional.ofNullable(crop);
  }

  public Optional<String> getShape() {
    return Optional.ofNullable(shape == null ? null : shape.name());
  }

  public Optional<Integer> getFirstPage() {
    return Optional.ofNullable(firstPage);
  }

  public Optional<Integer> getLastPage() {
    return Optional.ofNullable(lastPage);
  }

  public Optional<String> getLangTag() {
    return Optional.ofNullable(langTag);
  }

  enum Quality {
    @JsonProperty("lowest")
    LOWEST,

    @JsonProperty("low")
    LOW,

    @JsonProperty("medium")
    MEDIUM,

    @JsonProperty("high")
    HIGH,

    @JsonProperty("highest")
    HIGHEST
  }

  enum Format {
    @JsonProperty("jpeg")
    JPEG,

    @JsonProperty("png")
    PNG,
    @JsonProperty("gif")
    GIF
  }

  enum Shape {
    @JsonProperty("rounded")
    ROUNDED,

    @JsonProperty("rectangular")
    RECTANGULAR
  }
}
