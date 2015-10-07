package org.beangle.maven.plugin.patch

object Patches {

  var files = List("/org/hibernate/action/internal/CollectionUpdateAction.class", "/org/hibernate/collection/spi/PersistentCollection.class", "/org/hibernate/collection/internal/AbstractPersistentCollection.class")

  val PatchFlag = "/META-INF/beangle_patched.txt"
}
