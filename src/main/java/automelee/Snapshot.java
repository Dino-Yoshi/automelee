package automelee;

/** The state of one client tick, as the attack decision sees it. */
public final class Snapshot {

  private final boolean meleeItem;
  private final boolean attackKeyHeld;
  private final boolean usingItem;
  private final boolean rowing;
  private final boolean guiOpen;
  private final boolean spectator;
  private final boolean alive;
  private final float cooledStrength;
  private final boolean targetPresent;
  private final boolean targetLivingAndNotSelf;
  private final boolean clickSwingPlaying;

  private Snapshot(Builder b) {
    this.meleeItem = b.meleeItem;
    this.attackKeyHeld = b.attackKeyHeld;
    this.usingItem = b.usingItem;
    this.rowing = b.rowing;
    this.guiOpen = b.guiOpen;
    this.spectator = b.spectator;
    this.alive = b.alive;
    this.cooledStrength = b.cooledStrength;
    this.targetPresent = b.targetPresent;
    this.targetLivingAndNotSelf = b.targetLivingAndNotSelf;
    this.clickSwingPlaying = b.clickSwingPlaying;
  }

  public static Builder builder() {
    return new Builder();
  }

  boolean meleeItem() {
    return meleeItem;
  }

  boolean attackKeyHeld() {
    return attackKeyHeld;
  }

  boolean usingItem() {
    return usingItem;
  }

  boolean rowing() {
    return rowing;
  }

  boolean guiOpen() {
    return guiOpen;
  }

  boolean spectator() {
    return spectator;
  }

  boolean alive() {
    return alive;
  }

  float cooledStrength() {
    return cooledStrength;
  }

  boolean targetPresent() {
    return targetPresent;
  }

  boolean targetLivingAndNotSelf() {
    return targetLivingAndNotSelf;
  }

  boolean clickSwingPlaying() {
    return clickSwingPlaying;
  }

  public static final class Builder {

    private boolean meleeItem;
    private boolean attackKeyHeld;
    private boolean usingItem;
    private boolean rowing;
    private boolean guiOpen;
    private boolean spectator;
    private boolean alive;
    private float cooledStrength;
    private boolean targetPresent;
    private boolean targetLivingAndNotSelf;
    private boolean clickSwingPlaying;

    private Builder() {}

    public Builder meleeItem(boolean value) {
      this.meleeItem = value;
      return this;
    }

    public Builder attackKeyHeld(boolean value) {
      this.attackKeyHeld = value;
      return this;
    }

    public Builder usingItem(boolean value) {
      this.usingItem = value;
      return this;
    }

    public Builder rowing(boolean value) {
      this.rowing = value;
      return this;
    }

    public Builder guiOpen(boolean value) {
      this.guiOpen = value;
      return this;
    }

    public Builder spectator(boolean value) {
      this.spectator = value;
      return this;
    }

    public Builder alive(boolean value) {
      this.alive = value;
      return this;
    }

    public Builder cooledStrength(float value) {
      this.cooledStrength = value;
      return this;
    }

    public Builder targetPresent(boolean value) {
      this.targetPresent = value;
      return this;
    }

    public Builder targetLivingAndNotSelf(boolean value) {
      this.targetLivingAndNotSelf = value;
      return this;
    }

    public Builder clickSwingPlaying(boolean value) {
      this.clickSwingPlaying = value;
      return this;
    }

    public Snapshot build() {
      return new Snapshot(this);
    }
  }
}
