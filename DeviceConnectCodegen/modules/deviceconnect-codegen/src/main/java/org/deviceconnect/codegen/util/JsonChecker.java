/*
 JsonChecker.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.util;


import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;

import java.io.*;
import java.util.List;
import java.util.Map;

public final class JsonChecker {

    public static void main(final String[] args) {
        String dir = "/Users/mtaka/projects/docomo/dConnect-GitHub/DeviceConnect-Spec/api";
        JsonChecker checker = new JsonChecker();
        try {
            checker.checkExamples(new File(dir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int descriptionCount = 0;
    private int basePathCount = 0;
    private int examplesCount = 0;
    private int summaryCount = 0;

    public void checkDir(final File dir) throws IOException {
        if (!dir.exists()) {
            throw new IOException("dir is not found: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("dir is not a directory: " + dir.getAbsolutePath());
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".json");
            }
        });
        for (final File file : files) {
            check(file, new CheckListener() {
                @Override
                public void missing(final String jsonReference) {
                    print("miss: " + file.getName() + " - " + jsonReference);
                    if (jsonReference.contains("basePath")) {
                        basePathCount++;
                    }
                    if (jsonReference.contains("summary")) {
                        summaryCount++;
                    }
                    if (jsonReference.contains("description")) {
                        descriptionCount++;
                    }
                    if (jsonReference.contains("responses") || jsonReference.contains("x-event")) {
                        examplesCount++;
                    }
                }
            });
        }

        double minForCopyAndPaste = 0.25;
        double minForCreate = 10;
        double total = 0;
        print("JSON更新作業");
        print("・Wikiからコピーする部分: ");
        print("  basePath: " + basePathCount + " * " + minForCopyAndPaste + "分 = " + (minForCopyAndPaste * basePathCount) + "分");
        print("  summaries: " + summaryCount + " * " + minForCopyAndPaste + "分 = " + (minForCopyAndPaste * summaryCount) + "分");
        print("  descriptions: " + descriptionCount + " * " + minForCopyAndPaste + "分 = " + (minForCopyAndPaste * descriptionCount) + "分");
        print("・実際のレスポンスをコピー部分: ");
        print("  examples: " + examplesCount + " * " + minForCreate + "分 = " + (minForCreate * examplesCount) + "分");

        print("----------");

        total += (minForCopyAndPaste * basePathCount);
        total += (minForCopyAndPaste * summaryCount);
        total += (minForCopyAndPaste * descriptionCount);
        total += (minForCreate * examplesCount);
        print("計: " + total + "分 = " + (total / 60) + "時間 = " + (total / 60 / 8) + "人日");
    }

    void checkExamples(final File dir) throws IOException {
        if (!dir.exists()) {
            throw new IOException("dir is not found: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("dir is not a directory: " + dir.getAbsolutePath());
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".json");
            }
        });
        for (final File file : files) {
            print("File: " + file.getName());
            checkFile(file);
        }
    }

    private void checkFile(final File file) {
        final Counter count = new Counter();
        check(file, new CheckListener() {
            @Override
            public void missing(final String jsonReference) {
                print("miss: " + jsonReference);
            }
        });
    }

    private class Counter {
        private int value = 0;
        public void up() { value++; }
        public int value() { return this.value; }
        public void reset() {
            value = 0;
        }
    }

    private void print(String message) {
        System.out.println(message);
    }

    public void check(final File file, final CheckListener l) {
        Swagger swagger = new SwaggerParser().read(file.getAbsolutePath(), null, false);

        String basePath = swagger.getBasePath();
        if (basePath == null || basePath.equals("")) {
            l.missing("/basePath");
        }

        Info info = swagger.getInfo();
        if (info == null) {
            l.missing("/info");
        } else {
            String description = info.getDescription();
            if (description == null || description.equals("")) {
                l.missing("/info/description");
            }
        }
        Map<String, Path> paths = swagger.getPaths();
        if (paths != null) {
            for (Map.Entry<String, Path> pathEntry : paths.entrySet()) {
                String pathName = pathEntry.getKey().replaceAll("/", "~1");
                Path path = pathEntry.getValue();

                Map<HttpMethod, Operation> opMap = path.getOperationMap();
                if (opMap != null) {
                    for (Map.Entry<HttpMethod, Operation> opEntry : opMap.entrySet()) {
                        String method = opEntry.getKey().name().toLowerCase();
                        String pathToOp = "/paths/" + pathName + "/" + method;
                        Operation op = opEntry.getValue();

                        String summary = op.getSummary();
                        if (summary == null || summary.equals("")) {
                            l.missing(pathToOp + "/summary");
                        }
                        String description = op.getDescription();


                        List<Parameter> parameters = op.getParameters();
                        if (parameters != null) {
                            for (int i = 0; i < parameters.size(); i++) {
                                Parameter parameter = parameters.get(i);
                                String paramDescription = parameter.getDescription();
                                if (paramDescription == null || paramDescription.equals("")) {
                                    l.missing(pathToOp + "/parameters/" + i + "/description");
                                }
                            }
                        } else {
                            l.missing(pathToOp + "/parameters");
                        }

                        Map<String, Response> responses = op.getResponses();
                        if (responses != null) {
                            Response response200 = responses.get("200");
                            if (response200 != null) {
                                Property schema = response200.getSchema();
                                if (schema == null || !(schema instanceof RefProperty)) {
                                    l.missing(pathToOp + "/responses/200/schema");
                                }

                                Map<String, Object> examples = response200.getExamples();
                                if (examples != null) {
                                    Object example = examples.get("application/json");
                                    //System.out.println("++++++++ Example model class: " + example.getClass());
                                } else {
                                    l.missing(pathToOp + "/responses/200/examples");
                                }
                            } else {
                                l.missing(pathToOp + "/responses/200");
                            }
                        } else {
                            l.missing(pathToOp + "/responses");
                        }

                        String type = (String) op.getVendorExtensions().get("x-type");
                        if (type != null) {
                            if (type.equals("event") && "put".equalsIgnoreCase(method)) {
                                JsonNode event = (JsonNode) op.getVendorExtensions().get("x-event");
                                if (event != null) {
                                    JsonNode schema = event.get("schema");
                                    JsonNode examples = event.get("examples");
                                    if (schema == null) {
                                        l.missing(pathToOp + "/x-event/schema");
                                    }
                                    if (examples == null) {
                                        l.missing(pathToOp + "/x-event/examples");
                                    }
                                } else {
                                    l.missing(pathToOp + "/x-event");
                                }
                            } else if (type.equals("event") && "get".equalsIgnoreCase(method)) {
                                if (description == null || description.equals("")) {
                                    l.missing(pathToOp + "/description");
                                }
                            }
                        } else {
                            l.missing(pathToOp + "/x-type");
                        }
                    }
                }
            }
        } else {
            l.missing("/paths");
        }

    }

    private interface CheckListener {

        void missing(final String jsonReference);

    }
}