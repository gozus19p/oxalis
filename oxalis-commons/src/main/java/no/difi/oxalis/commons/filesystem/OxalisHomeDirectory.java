/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.filesystem;

import no.difi.oxalis.api.filesystem.HomeDetector;
import no.difi.oxalis.api.lang.OxalisLoadingException;
import no.difi.oxalis.commons.util.Sortables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Represents the Oxalis Home directory, which is located by inspecting various
 * places in the system in this order:
 * <p>
 * <ol>
 * <li>Directory referenced by Local JNDI Context <code>java:comp/env/OXALIS_HOME</code></li>
 * <li>Directory referenced by Java System Property <code>-D OXALIS_HOME</code></li>
 * <li>Directory referenced by Environment Variable <code>OXALIS_HOME</code></li>
 * <li>Directory <code>.oxalis</code> inside the home directory of the user</li>
 * </ol>
 *
 * @author steinar
 * @author thore
 */
public class OxalisHomeDirectory {

    private static final Logger log = LoggerFactory.getLogger(OxalisHomeDirectory.class);

    private List<HomeDetector> homeDetectors;

    public OxalisHomeDirectory(List<HomeDetector> homeDetectors) {
        this.homeDetectors = homeDetectors;
    }

    @SuppressWarnings("unused")
    public OxalisHomeDirectory() {
        this(StreamSupport.stream(ServiceLoader.load(HomeDetector.class).spliterator(), false)
                .sorted(Sortables.comparator())
                .collect(Collectors.toList()));
    }

    public File detect() {
    	//TODO: attualmente disabilitata
        File directory = new File("C:\\Users\\MGozzi\\.oxalis");
        /*directory = homeDetectors.stream()
                .sequential()
                .map(HomeDetector::detect)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new OxalisLoadingException(
                        "Unable to detect Oxalis home folder."));*/
        try {
            validateOxalisHomeDirectory(directory);
        } catch (OxalisLoadingException ex) {
            log.error(ex.getMessage());
            throw ex;
        }

        return directory;
    }

    private static void validateOxalisHomeDirectory(File oxalisHomeDirectory) {
        if (!oxalisHomeDirectory.exists()) {
            throw new OxalisLoadingException(oxalisHomeDirectory + " does not exist!");
        } else if (!oxalisHomeDirectory.isDirectory()) {
            throw new OxalisLoadingException(oxalisHomeDirectory + " is not a directory");
        } else if (!oxalisHomeDirectory.canRead()) {
            throw new OxalisLoadingException(oxalisHomeDirectory + " exists, is a directory but can not be read");
        }
    }
}
