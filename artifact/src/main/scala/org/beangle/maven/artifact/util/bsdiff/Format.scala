package org.beangle.maven.artifact.util.bsdiff

object Format {

  val Compression = "bzip2";

  //Size of the Header, in bytes.  4 fields * 8 bytes = 32 bytes
  val HeaderLength = 32;

  // Magic number to mark the start of a bsdiff header.
  val HeaderMagic = "BSDIFF40";

  /**
   * Data structure that encapsulates a bsdiff header.  The header is composed of
   * 8-byte fields, starting with the magic number "BSDIFF40."
   * <p/>
   * 0: BSDIFF40
   * 8: length of control block
   * 16: length of the diff block
   * 24: size of the output file
   */
  case class Header(controlLength: Int, diffLength: Int, outputLength: Int) {
    require(controlLength > 0 && diffLength > 0 && outputLength > 0)
  }

  case class Block(diffLength: Int, extraLength: Int, seekLength: Int)
}