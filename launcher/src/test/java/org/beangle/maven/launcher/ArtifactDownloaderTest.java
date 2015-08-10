package org.beangle.maven.launcher;

import java.util.ArrayList;
import java.util.List;

public class ArtifactDownloaderTest {
  ArtifactDownloader downloader = new ArtifactDownloader(new RemoteRepository(), new LocalRepository(
      LocalRepository.Maven2, "/tmp/repository"));

  public void testDownload() throws Exception {
    List<Artifact> artifacts = new ArrayList<Artifact>();
    artifacts.add(new Artifact("antlr", "antlr", "2.7.7"));
    artifacts.add(new Artifact("aopalliance", "aopalliance", "1.0"));
    artifacts.add(new Artifact("asm", "asm-commons", "3.3"));
    artifacts.add(new Artifact("asm", "asm-tree", "3.3"));
    artifacts.add(new Artifact("asm", "asm", "3.3"));
    artifacts.add(new Artifact("cglib", "cglib-nodep", "2.1_3"));
    artifacts.add(new Artifact("com.belerweb", "pinyin4j", "2.5.0"));
    artifacts.add(new Artifact("com.ckeditor", "ckeditor-java-core", "3.5.3"));
    artifacts.add(new Artifact("com.google.code.gson", "gson", "2.2.2"));
    artifacts.add(new Artifact("com.itextpdf.tool", "xmlworker", "5.5.0"));
    artifacts.add(new Artifact("com.itextpdf", "itext-asian", "5.2.0"));
    artifacts.add(new Artifact("com.itextpdf", "itextpdf", "5.5.0"));
    artifacts.add(new Artifact("com.jgeppert.struts2.jquery", "struts2-jquery-plugin", "3.6.1"));
    artifacts.add(new Artifact("com.jhlabs", "imaging", "01012005"));
    artifacts.add(new Artifact("com.octo.captcha", "jcaptcha-api", "1.0"));
    artifacts.add(new Artifact("com.octo.captcha", "jcaptcha", "1.0"));
    artifacts.add(new Artifact("com.yahoo.platform.yui", "yuicompressor", "2.4.7"));
    artifacts.add(new Artifact("commons-beanutils", "commons-beanutils", "1.8.2"));
    artifacts.add(new Artifact("commons-codec", "commons-codec", "1.7"));
    artifacts.add(new Artifact("commons-collections", "commons-collections", "3.2.1"));
    artifacts.add(new Artifact("commons-digester", "commons-digester", "2.0"));
    artifacts.add(new Artifact("commons-fileupload", "commons-fileupload", "1.2.2"));
    artifacts.add(new Artifact("commons-io", "commons-io", "2.2"));
    artifacts.add(new Artifact("commons-lang", "commons-lang", "2.1"));
    artifacts.add(new Artifact("commons-logging", "commons-logging", "1.1.1"));
    artifacts.add(new Artifact("dom4j", "dom4j", "1.6.1"));
    artifacts.add(new Artifact("javax.activation", "activation", "1.1"));
    artifacts.add(new Artifact("javax.mail", "mail", "1.4"));
    artifacts.add(new Artifact("javax.transaction", "jta", "1.1"));
    artifacts.add(new Artifact("javax.validation", "validation-api", "1.0.0.GA"));
    artifacts.add(new Artifact("jaxen", "jaxen", "1.1.6"));
    artifacts.add(new Artifact("jfree", "jcommon", "1.0.0"));
    artifacts.add(new Artifact("jfree", "jfreechart", "1.0.1"));
    artifacts.add(new Artifact("joda-time", "joda-time", "2.3"));
    artifacts.add(new Artifact("net.sf.ehcache", "ehcache-core", "2.4.3"));
    artifacts.add(new Artifact("net.sf.jxls", "jxls-core", "1.0.3"));
    artifacts.add(new Artifact("ognl", "ognl", "3.0.6"));
    artifacts.add(new Artifact("org.apache.ant", "ant-launcher", "1.8.4"));
    artifacts.add(new Artifact("org.apache.ant", "ant", "1.8.4"));
    artifacts.add(new Artifact("org.apache.commons", "commons-compress", "1.4"));
    artifacts.add(new Artifact("org.apache.commons", "commons-jexl", "2.0.1"));
    artifacts.add(new Artifact("org.apache.commons", "commons-lang3", "3.1"));
    artifacts.add(new Artifact("org.apache.poi", "poi-ooxml-schemas", "3.9"));
    artifacts.add(new Artifact("org.apache.poi", "poi-ooxml", "3.9"));
    artifacts.add(new Artifact("org.apache.poi", "poi", "3.9"));
    artifacts.add(new Artifact("org.apache.struts.xwork", "xwork-core", "2.3.15.1"));
    artifacts.add(new Artifact("org.apache.struts", "struts2-core", "2.3.15.1"));
    artifacts.add(new Artifact("org.apache.velocity", "velocity", "1.5"));
    artifacts.add(new Artifact("org.apache.xmlbeans", "xmlbeans", "2.3.0"));
    artifacts.add(new Artifact("org.beangle.commons", "beangle-commons-core", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.commons", "beangle-commons-model", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.commons", "beangle-commons-notification", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.commons", "beangle-commons-web", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.ems", "beangle-ems-core", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.ems", "beangle-ems-system", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.ems", "beangle-ems-web", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.ioc", "beangle-ioc-spring", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.orm", "beangle-orm-hibernate", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.security", "beangle-security-blueprint", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.security", "beangle-security-core", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.security", "beangle-security-web", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.struts2", "beangle-struts2-captcha", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.struts2", "beangle-struts2-convention", "3.4.14"));
    artifacts.add(new Artifact("org.beangle.struts2", "beangle-struts2-view", "3.4.14"));
    artifacts.add(new Artifact("org.beangle", "beangle-struts2-legacy", "2.4.0"));
    artifacts.add(new Artifact("org.beanshell", "bsh", "2.0b4"));
    artifacts.add(new Artifact("org.directwebremoting", "dwr", "2.0.10"));
    artifacts.add(new Artifact("org.freemarker", "freemarker", "2.3.20"));
    artifacts.add(new Artifact("org.hibernate.common", "hibernate-commons-annotations", "4.0.2.Final"));
    artifacts.add(new Artifact("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api", "1.0.1.Final"));
    artifacts.add(new Artifact("org.hibernate", "hibernate-core", "4.2.4.Final"));
    artifacts.add(new Artifact("org.hibernate", "hibernate-ehcache", "4.2.4.Final"));
    artifacts.add(new Artifact("org.hibernate", "hibernate-validator", "4.3.1.Final"));
    artifacts.add(new Artifact("org.javassist", "javassist", "3.15.0-GA"));
    artifacts.add(new Artifact("org.jboss.logging", "jboss-logging", "3.1.0.GA"));
    artifacts.add(new Artifact("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.1_spec",
        "1.0.1.Final"));
    artifacts.add(new Artifact("org.quartz-scheduler", "quartz", "1.8.5"));
    artifacts.add(new Artifact("org.slf4j", "slf4j-api", "1.7.7"));
    artifacts.add(new Artifact("org.springframework", "spring-aop", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-asm", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-beans", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-context-support", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-context", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-core", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-expression", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-jdbc", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.springframework", "spring-tx", "3.1.1.RELEASE"));
    artifacts.add(new Artifact("org.tukaani", "xz", "1.0"));
    artifacts.add(new Artifact("oro", "oro", "2.0.8"));
    artifacts.add(new Artifact("stax", "stax-api", "1.0.1"));
    artifacts.add(new Artifact("xml-apis", "xml-apis", "1.4.01"));

    downloader.download(artifacts.toArray(new Artifact[0]));
  }
}