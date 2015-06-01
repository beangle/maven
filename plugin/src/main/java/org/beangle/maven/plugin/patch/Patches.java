package org.beangle.maven.plugin.patch;

public class Patches {

  public static String[] files = { "/org/hibernate/action/internal/CollectionUpdateAction.class",
      "/org/hibernate/collection/spi/PersistentCollection.class" };

  public static final String PatchFlag = "/META-INF/beangle_patched.txt";

}
