/*
 * Copyright 2015 Wikidata Toolkit Developers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wikidata.wdtk.examples.dialog_extractor;

/*
 * #%L
 * Wikidata Toolkit Examples
 * %%
 * Copyright (C) 2014 - 2015 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.helpers.DatamodelConverter;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.datamodel.json.jackson.JacksonObjectFactory;
import org.wikidata.wdtk.datamodel.json.jackson.JsonSerializer;
import org.wikidata.wdtk.examples.ExampleHelpers;
import org.wikidata.wdtk.examples.JsonSerializationProcessor;

/**
 *
 * @author m9ra
 */
public class DiscoveringProcessor extends FilterableProcessor {

    static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    private final HashSet<String> includedIds = new HashSet<String>();

    private final HashSet<String> newLayerIds = new HashSet<>();

    public DiscoveringProcessor() {
        includedIds.addAll(this.allowedIds);
    }

    public void includeId(String id) {
        //start with president
        this.includedIds.add(id);
    }

    public void endLayer() {
        includedIds.addAll(newLayerIds);
        newLayerIds.clear();
    }

    @Override
    public void processItemDocument(ItemDocument itemDocument) {
        if (isConnected(itemDocument)) {
            super.processItemDocument(itemDocument);
        }
    }

    @Override
    protected void acceptedItemDocument(ItemDocument itemDocument) {
        logger.info("Document found: " + itemDocument.getItemId().getId());
        addAllowedIds(itemDocument);
    }

    public HashSet<String> getIds() {
        return new HashSet<String>(includedIds);
    }

    private void addAllowedIds(ItemDocument itemDocument) {
        newLayerIds.add(itemDocument.getItemId().getId());
        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            for (Statement s : sg.getStatements()) {
                Snak snak = s.getClaim().getMainSnak();
                if(!isAllowed(snak))
                    continue;
                
                if (snak instanceof ValueSnak) {
                    Value v = ((ValueSnak) snak).getValue();

                    if (v instanceof ItemIdValue) {
                        String id = ((ItemIdValue) v).getId();
                        newLayerIds.add(id);
                    }
                }
            }
        }
    }

    private boolean isConnected(ItemDocument itemDocument) {
        String documentId = itemDocument.getItemId().getId();        
        if (includedIds.contains(documentId)) {
            return true;
        }

        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            for (Statement s : sg.getStatements()) {

                Snak snak = s.getClaim().getMainSnak();
                if(!isAllowed(snak))
                    continue;
                
                if (snak instanceof ValueSnak) {
                    Value v = ((ValueSnak) snak).getValue();

                    if (v instanceof ItemIdValue) {
                        String id = ((ItemIdValue) v).getId();
                        if (includedIds.contains(id)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
