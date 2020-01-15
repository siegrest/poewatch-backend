package watch.poe.app.domain;

import com.google.gson.annotations.SerializedName;

public enum Rarity {
  // todo: fix this clusterfuck
  @SerializedName("0")
  Normal,
  @SerializedName("1")
  Magic,
  @SerializedName("2")
  Rare,
  @SerializedName("3")
  Unique,
  @SerializedName("4")
  Gem,
  @SerializedName("5")
  Currency,
  @SerializedName("6")
  Div,
  @SerializedName("7")
  Quest,
  @SerializedName("8")
  Prophecy,
  @SerializedName("9")
  Relic
}
