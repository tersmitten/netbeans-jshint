/*
 *  The MIT License
 *
 *  Copyright (c) 2011 by Stanislav Lomadurov <lord.rojer@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.lomatek.jshint;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.NativeArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Stanislav Lomadurov <lord.rojer@gmail.com>
 */
final public class JSHintRun {

    private static JSHintRun instance;
    /**
     * Rhino context
     * @url https://developer.mozilla.org/En/Rhino_documentation/Scopes_and_Contexts
     */
    private Context context = null;
    private Scriptable scope = null;

    /**
     *
     */
    JSHintRun() {
    }

    /**
     *
     * @return
     */
    public static JSHintRun getInstance() {
        if (instance == null) {
            instance = new JSHintRun();
        }
        return instance;
    }

    /**
     *
     * @param contents
     * @param file
     * @return
     */
    public List<JSHintIssue> run(String contents, FileObject file) {
        init();
        scope.put("contents", scope, contents);
        // Get options
        scope.put("opts", scope, JSHintOptions.getInstance().getOptions(context, scope, file));
        System.err.println("RUNNING JSHINT:");
        context.evaluateString(scope, "results = JSHINT(contents, opts);", "JSHint", 1, null);
        Scriptable lint = (Scriptable) scope.get("JSHINT", scope);
        // We leave out of context
        Context.exit();
        // Get JSHint errors
        NativeArray errors = (NativeArray) lint.get("errors", null);
        List<JSHintIssue> result = new ArrayList<JSHintIssue>();
        for (int i = 0; i < errors.getLength(); i++) {
            NativeObject error = (NativeObject) errors.get(i, null);
            if (null == error) {
                continue;
            }
            // Add error
            result.add(new JSHintIssue(error));
        }
        
        return result;
    }

    /**
     *
     */
    public void init() {
        try {
            context = Context.enter();
            context.setLanguageVersion(Context.VERSION_1_6);

            if (null == scope) {
                scope = context.initStandardObjects();
                Reader reader = new BufferedReader(
                    new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("org/lomatek/jshint/resources/jshint.js"), Charset.forName("UTF-8")
                    )
                );

                context.evaluateReader(scope, reader, "JSHint", 1, null);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
