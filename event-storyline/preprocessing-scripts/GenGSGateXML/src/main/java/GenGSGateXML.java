import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gate.Document;
import gate.Node;
import gate.util.GateException;
import org.w3c.dom.*;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.xml.sax.SAXException;
import gate.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GenGSGateXML {

    private static final Path annotatedTextFolder = Paths.get("/home/nadia/Documents/CLaC-Lab/TRE/Event-StoryLine/Code/GenGSGateXML/src/main/resources/v1.5/");
    private static final Path rawTextFolder = Paths.get("/home/nadia/Documents/CLaC-Lab/TRE/Event-StoryLine/Code/GenGSGateXML/src/main/resources/ecb+_naf/");
    private static final Path outputFolder = Paths.get("/home/nadia/Documents/CLaC-Lab/TRE/Event-StoryLine/Code/GenGSGateXML/output/");
    private static final HashMap<Path, List<Path>> rawTextFilePaths = new HashMap<>();

    private static void initializeRawTextFilePaths(){
        List<Path> dirPaths = null;
        try (Stream<Path> walk = Files.walk(rawTextFolder)) {
            dirPaths = walk.filter(Files::isDirectory)
                    .filter(f -> !f.equals(rawTextFolder))
                    .collect(Collectors.toList());
        }
        catch(java.io.IOException e){
            System.out.println(e.getMessage());
        }
        for(Path dirPath: dirPaths){
            try(Stream<Path> walk = Files.walk(dirPath)){
                rawTextFilePaths.put(dirPath, walk.filter(Files::isRegularFile)
                                        .collect(Collectors.toList()));
            }
            catch(java.io.IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    private static void generateOutputFolders(){
        rawTextFilePaths.keySet().stream()
                .forEach(path -> {
                    try {
                        Files.createDirectories(Paths.get(path.toString().replace(rawTextFolder.toString(), outputFolder.toString())));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void generateTextFilesAndGateDocument(){
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            for(List<Path> paths : rawTextFilePaths.values()) {
                for (Path path : paths) {
                    try {
                        System.out.println(path);
                        Path textFilePath = Paths.get(path.toString()
                                .replace(rawTextFolder.toString(), outputFolder.toString())
                                .replace(".xml.naf.fix.xml", ".txt"));
                        org.w3c.dom.Document doc = db.parse(new File(path.toString()));
                        doc.getDocumentElement().normalize();
                        String rawText = doc.getDocumentElement().getElementsByTagName("raw").item(0).getTextContent();
                        Files.write(textFilePath, rawText.getBytes(StandardCharsets.UTF_8));

                        Path xmlFilePath = Paths.get(path.toString()
                                .replace(rawTextFolder.toString(), outputFolder.toString())
                                .replace(".xml.naf.fix.xml", ".xml"));
                        gate.Document gateDoc = Factory.newDocument(rawText);

                        NodeList wfNodes = doc.getDocumentElement().getElementsByTagName("wf");
                        for(int i = 0; i < wfNodes.getLength(); i++){
                            NamedNodeMap atrributeMap = wfNodes.item(i).getAttributes();
                            String textContent = wfNodes.item(i).getTextContent();

                            gate.FeatureMap features = gate.Factory.newFeatureMap();
                            features.put("string", textContent);
                            features.put("id", atrributeMap.getNamedItem("id").getTextContent());
                            features.put("offset", atrributeMap.getNamedItem("offset").getTextContent());
                            features.put("length", atrributeMap.getNamedItem("length").getTextContent());
                            features.put("sent", atrributeMap.getNamedItem("sent").getTextContent());

                            Long startOffset = Long.parseLong(atrributeMap.getNamedItem("offset").getTextContent());
                            Long endOffset = startOffset + Long.parseLong(atrributeMap.getNamedItem("length").getTextContent());
                            gateDoc.getAnnotations().add(startOffset, endOffset, "GS_ECB+_NAF-WF", features);

                        }
                        Files.write(xmlFilePath, gateDoc.toXml().getBytes(StandardCharsets.UTF_8));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void annotateGateDocuments(){
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            for(List<Path> paths : rawTextFilePaths.values()) {
                for (Path path : paths) {
                    System.out.println(path);
                    try {
                        Path annotsPath = Paths.get(path.toString().
                                replace(rawTextFolder.toString(), annotatedTextFolder.toString())
                                .replace(".xml.naf.fix.xml", ".xml.xml")
                        );
                        org.w3c.dom.Document annotsDoc = db.parse(new File(annotsPath.toString()));
                        annotsDoc.getDocumentElement().normalize();

                        String xmlFilePath = path.toString()
                                .replace(rawTextFolder.toString(), outputFolder.toString())
                                .replace(".xml.naf.fix.xml", ".xml");
                        gate.Document gateDoc = Factory.newDocument(new File(xmlFilePath).toURI().toURL());

                        List<Annotation> wfAnnList = gateDoc.getAnnotations().get("GS_ECB+_NAF-WF").inDocumentOrder();
                        org.w3c.dom.NodeList tokenNodes = annotsDoc.getElementsByTagName("token");

                        for(int i = 0; i < tokenNodes.getLength(); i++){
                            org.w3c.dom.NamedNodeMap attributeMap = tokenNodes.item(i).getAttributes();
                            if(tokenNodes.item(i).getTextContent().strip().equals(gate.Utils.cleanStringFor(gateDoc, wfAnnList.get(i)).strip())){
                                gate.FeatureMap features = gate.Factory.newFeatureMap();
                                features.put("string", tokenNodes.item(i).getTextContent());
                                features.put("t_id", attributeMap.getNamedItem("t_id").getTextContent());
                                features.put("sentence", attributeMap.getNamedItem("sentence").getTextContent());
                                features.put("number", attributeMap.getNamedItem("number").getTextContent());
                                gateDoc.getAnnotations().add(wfAnnList.get(i).getStartNode(), wfAnnList.get(i).getEndNode(), "GS_TOKEN", features);
                            }
                        }

                        Files.write(Paths.get(xmlFilePath), gateDoc.toXml().getBytes(StandardCharsets.UTF_8));

                        org.w3c.dom.NodeList markableNodes = annotsDoc.getElementsByTagName("Markables").item(0).getChildNodes();
                        for(int i = 0; i < markableNodes.getLength(); i++){
                            if(!markableNodes.item(i).getNodeName().equals("#text")){

                                org.w3c.dom.NodeList tokenAnchorNodes = markableNodes.item(i).getChildNodes();
                                ArrayList<String> tokenAnchorIds = new ArrayList<String>();
                                for(int j = 0; j < tokenAnchorNodes.getLength() ;j++){
                                    if (tokenAnchorNodes.item(j).getNodeName().equals("token_anchor")) {
                                        tokenAnchorIds.add(tokenAnchorNodes.item(j).getAttributes().getNamedItem("t_id").getTextContent());
                                    }
                                }
                                if(tokenAnchorIds.size() > 0){
                                    String startTokenId = tokenAnchorIds.get(0);
                                    String endTokenId = tokenAnchorIds.get(tokenAnchorIds.size() - 1);

                                    gate.Node startAnnNode = null;
                                    gate.Node endAnnNode = null;

                                    List<Annotation> tokenAnnList = gateDoc.getAnnotations().get("GS_TOKEN").inDocumentOrder();
                                    for(Annotation tokenAnn: tokenAnnList){
                                        if(tokenAnn.getFeatures().get("t_id").equals(startTokenId)){
                                            startAnnNode = tokenAnn.getStartNode();
                                        }
                                        if(tokenAnn.getFeatures().get("t_id").equals(endTokenId)){
                                            endAnnNode = tokenAnn.getEndNode();
                                            break;
                                        }
                                    }
                                    gate.FeatureMap features = gate.Factory.newFeatureMap();
                                    org.w3c.dom.NamedNodeMap attributeNodes = markableNodes.item(i).getAttributes();
                                    for(int j = 0; j < attributeNodes.getLength() ;j++){
                                        features.put(attributeNodes.item(j).getNodeName(), attributeNodes.item(j).getNodeValue());
                                    }
                                    String annName = "GS_" + markableNodes.item(i).getNodeName();
                                    gateDoc.getAnnotations().add(startAnnNode, endAnnNode, annName, features);
                                }
                            }
                        }
                        org.w3c.dom.NodeList relationNodes = annotsDoc.getElementsByTagName("Relations").item(0).getChildNodes();

                        for(int i = 0; i < relationNodes.getLength(); i++){
                            if(!relationNodes.item(i).getNodeName().equals("#text")){
                                org.w3c.dom.NodeList childNodes = relationNodes.item(i).getChildNodes();
                                String sourceId = null;
                                String targetId = null;
                                for(int j = 0; j < childNodes.getLength() ;j++){
                                    if (childNodes.item(j).getNodeName().equals("source")) {
                                        sourceId = childNodes.item(j).getAttributes().getNamedItem("m_id").getTextContent();
                                    }
                                    if (childNodes.item(j).getNodeName().equals("target")) {
                                        targetId = childNodes.item(j).getAttributes().getNamedItem("m_id").getTextContent();
                                    }
                                }

                                gate.Annotation sourceAnn = null;
                                gate.Annotation targetAnn = null;

                                List<Annotation> annList = gateDoc.getAnnotations().inDocumentOrder();
                                for(Annotation ann: annList){
                                    if(ann.getFeatures().containsKey("m_id")){
                                        if(ann.getFeatures().get("m_id").equals(sourceId)){
                                            sourceAnn = ann;
                                        }
                                        if(ann.getFeatures().get("m_id").equals(targetId)){
                                            targetAnn = ann;
                                        }
                                    }else{
                                        continue;
                                    }
                                    if(targetAnn != null && sourceAnn != null){
                                        break;
                                    }
                                }
                                gate.FeatureMap features = gate.Factory.newFeatureMap();
                                org.w3c.dom.NamedNodeMap attributeNodes = relationNodes.item(i).getAttributes();
                                for(int j = 0; j < attributeNodes.getLength() ;j++){
                                    features.put(attributeNodes.item(j).getNodeName(), attributeNodes.item(j).getNodeValue());
                                }
                                features.put("sourceAnn", sourceAnn.getFeatures());
                                String annName = "GS_" + relationNodes.item(i).getNodeName();
                                gateDoc.getAnnotations().add(targetAnn.getStartNode(), targetAnn.getEndNode(), annName, features);
                                //gateDoc.getAnnotations().getRelations().addRelation(annName, new int[]{sourceAnn.getId(), targetAnn.getId()});
                            }
                        }
                        Files.write(Paths.get(xmlFilePath), gateDoc.toXml().getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void  main(String[] args) throws GateException {
        Gate.init();
        initializeRawTextFilePaths();
        generateOutputFolders();
        generateTextFilesAndGateDocument();
        annotateGateDocuments();
    }
}
