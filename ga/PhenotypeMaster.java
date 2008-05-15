package ga;

import java.util.Map;
import java.util.TreeMap;

/** A class that is responsible for adding, removing, and
 * storing genetic linkages between agent properties and
 * genes. Currently serves more as a repository of info 
 * for other classes than anything else.
 */

public class PhenotypeMaster {
  
  /** Number of genes. */
  public static final int NUM_GENES = 3;
  
  /** The position of the gene linked to red colour phenotype. */
  public static final int RED_PHENOTYPE = 0;
  
  /** The position of the gene linked to green colour phenotype. */
  public static final int GREEN_PHENOTYPE = 1;
  
  /** The position of the gene linked to blue colour phenotype. */
  public static final int BLUE_PHENOTYPE = 2;
  
  /** Stores which phenotype is linked to what gene position. */
  public static String[] linked_phenotypes = {"", "", ""};
  
  /**
	 * The curves of each gene. Since it is difficult to parse a function, the
	 * 256 slots are to store 256 data points of a function. These data points
	 * are supposed to be coefficients that will fit the default values of
	 * attributes in question to the corresponding desired curves.
	 */
  public static Map<String,double[]> curves = new TreeMap<String,double[]>();
  
  /** Stores which gene position is linked to which attribute. */
  public static Map<String,Integer> genetic_linkages = new TreeMap<String,Integer>();
  
  /**
	 * The possible physical attributes/parameters that can be linked to one of
	 * the gene loci.
	 */
  public static final String[] mutable = {
    "Mutation Rate",
    "Initial Energy",
    "Favourite Food Energy",
    "Other Food Energy",
    "Breed Energy", 
    "Pregnancy Period - 1 parent", 
    "Step Energy Loss", 
    "Step Rock Energy Loss",
    "Turn Right Energy Loss",
    "Turn Left Energy Loss",
    "Min. Communication Similarity",
    "Step Agent Energy Loss",
    "Pregnancy Period - 2 parents",
    "Min. Breed Similarity",
    "2 parents Breed Chance",
    "1 parent Breed Chance",
    "Aging Limit",
    "Aging Rate", 
    "PD Cooperation Probability",
    "Broadcast fixed range",
    "Broadcast Minimum Energy",
    "Broadcast Energy Cost"};
  
  /**
	 * Links a physical attribute to a gene locus.
	 * 
	 * @param attri
	 *            Attribute in question
	 * @param gene_position
	 *            Position of locus
	 * @throws GeneticCodeException
	 */
  public static void setLinkedAttributes(String attri, int gene_position) throws GeneticCodeException {
    if (gene_position > 3 || gene_position < 0) {
      throw new GeneticCodeException("setLinkedAttributes: Out of bound input.");
    } else { 
      removeLinkedAttributes(gene_position);
      genetic_linkages.put(attri, gene_position);
      linked_phenotypes[gene_position] = attri;
    }
  }
  
  /** 
   * Removes the link between the attribute linked to the 
   * specified gene locus and the gene itself.
   * 
   * @param gene_position Gene locus of interest
   * @throws GeneticCodeException
   */
  public static void removeLinkedAttributes(int gene_position) throws GeneticCodeException {
    if (gene_position > 3 || gene_position < 0) {
      throw new GeneticCodeException("setLinkedAttributes: Out of bound input.");
    } else if (linked_phenotypes[gene_position].equals("")) { 
      // Do nothing;
    } else {
      genetic_linkages.remove(linked_phenotypes[gene_position]);
      linked_phenotypes[gene_position] = "";
    }
  }
  
  /**
	 * Links a curve to a gene locus.
	 * 
	 * @param curve
	 *            Curve in question
	 * @param gene_position
	 *            Position of locus
	 * @throws GeneticCodeException
	 */
  public static void setCurves(String attri, double[] curve) throws GeneticCodeException {
    if (curve.length == 256 && genetic_linkages.get(attri) != null) {     
      curves.put(attri, curve);
    } else if (curve.length != 256) {
      throw new GeneticCodeException("setCurves: Invalid curves.");
    } else {
      throw new GeneticCodeException("setCurves: Bad attribute input. ");
    }
  }
}
    
  