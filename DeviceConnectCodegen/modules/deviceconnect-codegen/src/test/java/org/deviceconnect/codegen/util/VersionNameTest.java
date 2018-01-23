package org.deviceconnect.codegen.util;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class VersionNameTest {

    @Test
    public void testParse_Success() {
        VersionName versionName = VersionName.parse("1.2.3");
        assertNotNull(versionName);
    }

    @Test
    public void testParse_Error_TooLong() {
        VersionName versionName = VersionName.parse("1.2.3.4");
        assertNull(versionName);
    }

    @Test
    public void testParse_Error_TooShort() {
        VersionName versionName = VersionName.parse("1.2");
        assertNull(versionName);
    }

    @Test
    public void testIsEqualOrMoreThan_True_SameVersion_SameObj() {
        VersionName ver123 = VersionName.parse("1.2.3");
        assertTrue(ver123.isEqualOrMoreThan(ver123));
    }

    @Test
    public void testIsEqualOrMoreThan_True_SameVersion_NotSameObj() {
        VersionName ver123_a = VersionName.parse("1.2.3");
        VersionName ver123_b = VersionName.parse("1.2.3");
        assertTrue(ver123_a.isEqualOrMoreThan(ver123_b));
    }

    @Test
    public void testIsEqualOrMoreThan_True_MoreVersion() {
        VersionName ver123 = VersionName.parse("1.2.3");
        VersionName ver456 = VersionName.parse("4.5.6");
        assertTrue(ver456.isEqualOrMoreThan(ver123));
    }

    @Test
    public void testIsEqualOrMoreThan_False() {
        VersionName ver123 = VersionName.parse("1.2.3");
        VersionName ver456 = VersionName.parse("4.5.6");
        assertFalse(ver123.isEqualOrMoreThan(ver456));
    }
}
