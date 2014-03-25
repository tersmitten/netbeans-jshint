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
import java.util.Iterator;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Stanislav Lomadurov <lord.rojer@gmail.com>
 */
public class JSHintOptions {

    private static JSHintOptions INSTANCE;
    private static Scriptable options = null;

    /**
     *
     * @return
     */
    public static JSHintOptions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JSHintOptions();
        }

        return INSTANCE;
    }

    /**
     * Get the Rhino context contains options for JSHint.
     *
     * @param context
     * @param scope
     * @param file
     * @return
     */
    public Scriptable getOptions(Context context, Scriptable scope, FileObject file) {
        if (null != options) return options;
        options = context.newObject(scope);

        String optionText = getOptionsFile(file);
        System.err.println("JSHint options: " + optionText);
        if (null == optionText) return options;

        JSONObject json = (JSONObject) JSONValue.parse(optionText);
        Iterator iter = json.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry) iter.next();
            options.put(entry.getKey(), options, entry.getValue());
        }

        return options;
    }

    /**
     * 
     * @param sourceFile
     * @return
     */
    public String getOptionsFile(FileObject sourceFile) {
        if (sourceFile.isFolder()) {
            FileObject optionsFile = sourceFile.getFileObject(".jshintrc");
            if (optionsFile != null) {
                try {
                    return optionsFile.asText();
                } catch (IOException ex) {
                    return null;
                }
            }
            if (!sourceFile.isRoot()) {
                return getOptionsFile(sourceFile.getParent());
            }
            return null;
        }
        return getOptionsFile(sourceFile.getParent());
    }
}
