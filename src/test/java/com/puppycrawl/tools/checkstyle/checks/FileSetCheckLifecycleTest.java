////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.checks.imports.AvoidStarImportCheck;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

public class FileSetCheckLifecycleTest
    extends AbstractModuleTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/misc/fileset";
    }

    @Test
    public void testTranslation() throws Exception {
        final Configuration checkConfig =
            createModuleConfig(TestFileSetCheck.class);
        final String[] expected = CommonUtils.EMPTY_STRING_ARRAY;
        verify(checkConfig, getPath("InputFileSetCheckLifecycleIllegalTokens.java"), expected);

        assertTrue("destroy() not called by Checker", TestFileSetCheck.isDestroyed());
    }

    @Test
    public void testProcessCallsFinishBeforeCallingDestroy() throws Exception {

        final DefaultConfiguration dc = new DefaultConfiguration("configuration");
        final DefaultConfiguration twConf = createModuleConfig(TreeWalker.class);
        dc.addAttribute("charset", "UTF-8");
        dc.addChild(twConf);
        twConf.addChild(new DefaultConfiguration(AvoidStarImportCheck.class.getName()));

        final Checker checker = new Checker();
        final Locale locale = Locale.ROOT;
        checker.setLocaleCountry(locale.getCountry());
        checker.setLocaleLanguage(locale.getLanguage());
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(dc);
        checker.addListener(getBriefUtLogger());

        checker.addFileSetCheck(new TestFileSetCheck());

        final String[] expected = CommonUtils.EMPTY_STRING_ARRAY;

        verify(checker, getPath("InputFileSetCheckLifecycleIllegalTokens.java"), expected);

        assertTrue("FileContent should be available during finishProcessing() call",
                TestFileSetCheck.isFileContentAvailable());
    }

    private static class TestFileSetCheck extends AbstractFileSetCheck {
        private static boolean destroyed;
        private static boolean fileContentAvailable;
        private static FileContents contents;

        @Override
        public void destroy() {
            destroyed = true;
        }

        public static boolean isDestroyed() {
            return destroyed;
        }

        public static boolean isFileContentAvailable() {
            return fileContentAvailable;
        }

        @Override
        protected void processFiltered(File file, FileText fileText) {
            contents = new FileContents(fileText);
        }

        @Override
        public void finishProcessing() {
            fileContentAvailable = contents != null;
        }
    }
}
