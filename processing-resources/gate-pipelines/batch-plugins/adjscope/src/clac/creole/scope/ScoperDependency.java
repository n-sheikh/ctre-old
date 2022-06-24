package clac.creole.scope;
/**
 * Representation of a syntactic dependency for Scoper. Based on the GATE
 * Parser-Stanford SentenceDependency class, with the addition that it allows
 * dependencies in both directions. If the isGov flag is set to true, this
 * dependency belongs to the governor, and the target is the dependant.
 * If isGov is false, the roles are reversed.
 *
 * @author ma_fauch, CLaC 2014
 */

/**
 * Simple data structure representing a single dependency relation.  The "target"
 * is the Annotation ID of the dependent if isGov is true, or the Annotation ID of
 * the governor if isGov is false; the "type" is the dependency 
 * tag (<a href="http://nlp.stanford.edu/software/parser-faq.shtml#c">the
 * Stanford Parser documentation</a> contains links to the tagset</a>; for example,
 * nsubj = "nominal subject", dobj = "direct object).
 */
public class ScoperDependency {

    /**
     * The type of the dependency relation (det, amod, etc.).
     */
    private String type;

    /**
     * The ID of the token that is the target of this relation.
     */
    private Integer targetId;

    /**
     * The direction of the dependency (whether the owner is governor)
     */
    private boolean isGov;

    public ScoperDependency(String type, Integer targetId, boolean isGov) {
        this.type = type;
        this.targetId = targetId;
        this.isGov = isGov;
    }

    /**
     * Return the dependency tag (type).
     * @return the dependency tag
     */
    public String getType() {
        return type;
    }

    /**
     * Set the dependency tag.
     * @param type dependency tag
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Return the GATE Annotation ID of the dependent.
     * @return the Annotation ID
     */
    public Integer getTargetId() {
        return targetId;
    }

    /**
     * Set the Annotation ID of the dependent.
     * @param targetId the Annotation ID
     */
    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    /**
     * Set the dependency direction.
     * @param if this dependency's owner is the governor
     */
    public boolean isGov() {
        return isGov;
    }

    /**
     * Set the dependency direction.
     * @param isGov the dependency direction
     */
    public void setIsGov(boolean isGov) {
        this.isGov = isGov;
    }

    /**
     * Format the data structure for display.
     * For example, if type is "dobj" and the dependent has Annotation ID 37,
     * return the String "dobj(37)". 
     */
    public String toString() {
        if (isGov) {
            return type + "(" + targetId + ")";
       } else {
            return "*" + type + "(" + targetId + ")";
       }
    }
}
