package io.github.atkawa7.pom;

import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

final class DomUtilities {

    /**
     * Construction prohibited.
     */
    private DomUtilities() {

    }

    public static Element getChildElement(Element element, String... tagnames)
            throws MojoExecutionException {

        return getChildElement(element, tagnames, tagnames.length);
    }

    public static String getChildElementValue(Element element, String... tagnames)
            throws MojoExecutionException {

        Element childElement = getChildElement(element, tagnames, tagnames.length);
        if (childElement != null) {
            return childElement.getTextContent();
        }
        return null;
    }

    public static List<Element> getChildElements(Element element, String... xmlPath) throws MojoExecutionException {

        Element parent = getChildElement(element, xmlPath, xmlPath.length - 1);
        if (parent == null) {
            return null;
        }
        String tagname = xmlPath[xmlPath.length - 1];
        List<Element> result = new ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if (childElement.getTagName().equals(tagname)) {
                    result.add(childElement);
                }
            }
        }
        return result;
    }

    private static Element getChildElement(Element element, String[] tagnames, int length)
            throws MojoExecutionException {

        for (int xpathIndex = 0; xpathIndex < length; xpathIndex++) {
            String tagname = tagnames[xpathIndex];
            NodeList children = element.getChildNodes();
            Element nextElement = null;
            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    if (childElement.getTagName().equals(tagname)) {
                        nextElement = childElement;
                        break;
                    }
                }
            }
            if (nextElement == null) {
                return null;
            }
            element = nextElement;
        }
        return element;
    }

}
