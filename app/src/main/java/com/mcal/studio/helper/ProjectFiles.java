package com.mcal.studio.helper;

class ProjectFiles {

    static class Default {

        /**
         * HTML BareBones Template
         */
        static final String INDEX = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>@appName</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <meta name=\"appPackageName\" content=\"@appPackageName\">\n" +
                "    <link rel=\"stylesheet\" href=\"css/style.css\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Hello World!</h1>\n" +
                "    <script src=\"js/main.js\"></script>\n" +
                "  </body>\n" +
                "</html>";

        static String getIndex(String name, String packageName) {
            return INDEX.replace("@appName", name).replace("@appPackageName", packageName);
        }
    }

    static class Import {

        /**
         * HTML BareBones Template
         */
        static final String INDEX = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>@appName</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <meta name=\"appPackageName\" content=\"@appPackageName\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Hello World!</h1>\n" +
                "  </body>\n" +
                "</html>";

        static String getIndex(String name, String packageName) {
            return INDEX.replace("@appName", name).replace("@appPackageName", packageName);
        }
    }
}
