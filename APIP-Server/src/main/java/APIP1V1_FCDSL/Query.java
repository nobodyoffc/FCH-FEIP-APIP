package APIP1V1_FCDSL;

public class Query{

    protected String[] exists;
    protected String[] unexists;
    protected Terms terms;
    protected Part part;
    protected Match match;
    protected Range range;
    protected Equals equals;

    public String[] getExists() {
        return exists;
    }

    public void setExists(String[] exists) {
        this.exists = exists;
    }

    public String[] getUnexists() {
        return unexists;
    }

    public void setUnexists(String[] unexists) {
        this.unexists = unexists;
    }

    public Terms getTerms() {
        return terms;
    }

    public void setTerms(Terms terms) {
        this.terms = terms;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public Equals getEquals() {
        return equals;
    }

    public void setEquals(Equals equals) {
        this.equals = equals;
    }
}
