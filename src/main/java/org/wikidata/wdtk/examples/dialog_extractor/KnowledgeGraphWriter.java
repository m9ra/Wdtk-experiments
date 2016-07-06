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
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import static org.wikidata.wdtk.examples.dialog_extractor.DiscoveringProcessor.logger;

/**
 *
 * @author m9ra
 */
public class KnowledgeGraphWriter extends FilterableProcessor {

    private final HashSet<String> includedDocuments = new HashSet<String>();

    private final FileWriter writer;

    private int _temporaryNodesCounter = 0;

    public KnowledgeGraphWriter(String resultFile, HashSet<String> includedIds) throws IOException {
        writer = new FileWriter(resultFile);
        includedDocuments.addAll(includedIds);
    }

    @Override
    public void processItemDocument(ItemDocument itemDocument) {
        if (includedDocuments.contains(itemDocument.getItemId().getId())) {
            super.processItemDocument(itemDocument);
        }
    }

    @Override
    protected void acceptedItemDocument(ItemDocument itemDocument) {
        try {
            writeData(itemDocument);
        } catch (IOException ex) {
            Logger.getLogger(KnowledgeGraphWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeData(ItemDocument itemDocument) throws IOException {
        String sourceId = itemDocument.getItemId().getId();

        Map<String, MonolingualTextValue> labels = itemDocument.getLabels();
        if (labels.containsKey("en")) {
            write(sourceId, "en.label", labels.get("en").getText());
        }

        Map<String, List<MonolingualTextValue>> aliases = itemDocument.getAliases();
        if (aliases.containsKey("en")) {
            for (MonolingualTextValue alias : aliases.get("en")) {
                write(sourceId, "en.alias", alias.getText());
            }
        }

        for (StatementGroup sg : itemDocument.getStatementGroups()) {
            String propertyId = sg.getProperty().getId();
            for (Statement s : sg.getStatements()) {
                Snak snak = s.getClaim().getMainSnak();
                if (!isAllowed(snak)) {
                    continue;
                }

                if (snak instanceof ValueSnak) {
                    Value v = ((ValueSnak) snak).getValue();

                    if (v instanceof ItemIdValue) {
                        String targetId = ((ItemIdValue) v).getId();

                        if (includedDocuments.contains(targetId)) {
                            writeWithQualifiers(sourceId, propertyId, targetId, s.getClaim().getQualifiers());
                        }
                    } else {
                        writeScalar(sourceId, propertyId, v);
                    }
                }
            }
        }
    }

    private void writeWithQualifiers(String sourceId, String propertyId, String targetId, List<SnakGroup> qualifiers) throws IOException {

        String temporaryNode = getUniqueTemporaryNodeName();
        boolean wasNodeUsed = false;
        for (SnakGroup qualifier : qualifiers) {
            String qualifierId = qualifier.getProperty().getId();
            for (Snak snak : qualifier) {
                if (!(snak instanceof ValueSnak)) {
                    continue;
                }
                Value v = ((ValueSnak) snak).getValue();

                wasNodeUsed = wasNodeUsed | writeScalar(temporaryNode, qualifierId, v);
            }
        }

        if (wasNodeUsed) {
            write(temporaryNode, "main", targetId);
            write(sourceId, propertyId, temporaryNode);
        } else {
            write(sourceId, propertyId, targetId);
            //TODO this is a hack - because of not wasting with ids
            _temporaryNodesCounter -= 1;
        }
    }

    private boolean writeScalar(String sourceId, String propertyId, Value v) throws IOException {
        String valueRepresentation;
        if (v instanceof StringValue) {
            valueRepresentation = ((StringValue) v).getString();
            return false; //dont write this for now
        } else if (v instanceof QuantityValue) {
            valueRepresentation = ((QuantityValue) v).getNumericValue().toString();
            return false; //dont write this for now
        } else if (v instanceof TimeValue) {
            TimeValue t = ((TimeValue) v);
            valueRepresentation = "" + t.getYear() + "-" + t.getMonth() + "-" + t.getDay();
        } else {
            return false;
        }

        write(sourceId, propertyId, valueRepresentation);
        return true;
    }

    private String getUniqueTemporaryNodeName() {
        _temporaryNodesCounter += 1;
        return "$" + _temporaryNodesCounter;
    }

    private void write(String sourceId, String propertyId, String targetValueRepresentation) throws IOException {
        String edgeRepresentation = propertyId;
        writer.write(sourceId);
        writer.write(";");
        writer.write(edgeRepresentation);
        writer.write(";");
        writer.write(targetValueRepresentation.replace("\n", " ").replace("\r", " "));
        writer.write("\n");
    }

    public void close() throws IOException {
        writer.close();
    }

}
