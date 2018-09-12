/*
 ResourceBundleWithUtf8.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * UTF-8 エンコーディングされたプロパティファイルを {@link ResourceBundle} クラスで取り扱う。
 *
 * 下記のクラスを複製した:
 * https://gist.github.com/komiya-atsushi/0294e4a2fcffabe1b3e2#file-propertieswithutf8-java
 */
public class ResourceBundleWithUtf8 {

    public static final ResourceBundle.Control UTF8_ENCODING_CONTROL = new ResourceBundle.Control() {
        /**
         * UTF-8 エンコーディングのプロパティファイルから ResourceBundle オブジェクトを生成します。
         * <p>
         * 参考 :
         * <a href="http://jgloss.sourceforge.net/jgloss-core/jacoco/jgloss.util/UTF8ResourceBundleControl.java.html">
         * http://jgloss.sourceforge.net/jgloss-core/jacoco/jgloss.util/UTF8ResourceBundleControl.java.html
         * </a>
         * </p>
         *
         * @throws IllegalAccessException
         * @throws InstantiationException
         * @throws IOException
         */
        @Override
        public ResourceBundle newBundle(final String baseName, final Locale locale,
                                        final String format, final ClassLoader loader,
                                        final boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");

            try (InputStream is = loader.getResourceAsStream(resourceName);
                 InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                 BufferedReader reader = new BufferedReader(isr)) {
                return new PropertyResourceBundle(reader);
            }
        }
    };
}