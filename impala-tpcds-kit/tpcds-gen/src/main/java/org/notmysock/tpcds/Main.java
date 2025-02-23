package org.notmysock.tpcds;

import org.apache.hadoop.fs.Path;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        Path dsdgen = new Path("dsdgen/tools/tpcds.jar");
        URI dsuri = dsdgen.toUri();
        URI link = new URI(dsuri.getScheme(),
                dsuri.getUserInfo(), dsuri.getHost(),
                dsuri.getPort(), dsuri.getPath(),
                dsuri.getQuery(), "tpcds.jar");
        System.out.println(link);
    }
}
