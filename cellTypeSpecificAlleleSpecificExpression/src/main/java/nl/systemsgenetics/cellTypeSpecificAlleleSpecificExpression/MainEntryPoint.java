/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.systemsgenetics.cellTypeSpecificAlleleSpecificExpression;

import java.text.NumberFormat;
import static nl.systemsgenetics.cellTypeSpecificAlleleSpecificExpression.ReadGenoAndAsFromIndividual.readGenoAndAsFromIndividual;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.jdom.IllegalDataException;
import org.molgenis.genotype.GenotypeInfo;


/**
 *
 * @author Adriaan van der Graaf
 */


public class MainEntryPoint {
    /**
     * This is the entrypoint for cell type specific ASE,
     * This will provide the logic, based on what to do based on command line statements.
     * @param args
    */
    private static final Logger LOGGER;
    private static final Options OPTIONS;
    
    public static final NumberFormat DEFAULT_NUMBER_FORMATTER = NumberFormat.getInstance();

    static {

		LOGGER = Logger.getLogger(GenotypeInfo.class);

		OPTIONS = new Options();

		Option option;

                /*
                    Required Arguments (not optionally)
                */
                
		option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("Determine what to do in this program, currently the following options are available:\n"+
                                                 "\t1\tDetermine Allele specific reads per SNP: ASreads\n\n" +  
                                                 "\t2\tASE test per SNP                         ASEperSNP\n" + 
                                                 "\t3\tBeta binomial based on phasing           ASEperRegion\n\n" +
                                                 "Please Run an option based on the number in the first column, or through the name in the third column."
                                                )
				.withLongOpt("action")
				.isRequired()
				.create('A');
		OPTIONS.addOption(option);
                
                option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("location of file in which to write output.\n "
                                               + "When action is: ASreads, this will be the output of as_file\n"
                                               + "When action is ASEperRegion, it will provide the test statistics using the basefilename and adding an extension\n"
                                               + "Dispersion will be saved as: <output>_dispersionFile.txt\n"
                                               + "Binomial reults will be saved as: <output>_BinomialResults.txt\n"                                               
                                               + "Beta binomial results will be saved as: <output>_BetaBinomialResults.txt\n"
                                               + "CTS binomial results will be saved as: <output>_CTSBinomialResults.txt\n"
                                               + "CTS beta binomial results will be saved as: <output>_CTSBetaBinomialResults.txt\n")
				.withLongOpt("output")
                                .isRequired()
				.create('O');
		OPTIONS.addOption(option);
                
                /*
                    Arguments that are conditionally required 
                        (based on the specific action that is being done)
                */
                
                option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("location of file where coupling data is stored.\n "
                                               + "Required when action is: ASreads\n"
                                               + "Coupling data will have the individual name in the first column and\n"
                                               + "the associated sample bam file name (without path and extenstion) in the second column")                                                
				.withLongOpt("coupling_file")
				.create('C');
		OPTIONS.addOption(option);
                
                option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("Path to directory or location of file from which to load genotype data.\n"
                                                + "Currently only TriTyper and tabix indexed vcf data is available"
                                                + "Required when action is: ASreads")                                                
				.withLongOpt("genotype_location")
				.create('G');
		OPTIONS.addOption(option);
                
                
                option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("Location of bamfile from which to load data.\n "
                                               + "Required when action is: ASreads")                                                
				.withLongOpt("bam_file")
				.create('B');
		OPTIONS.addOption(option);
                
                option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("Location of  a file containing paths from which to load as data created in .\n "
                                               + "Required when action is: ASEperSNP, ASEperRegion.")                                                
				.withLongOpt("as_locations")
				.create('L');
		OPTIONS.addOption(option);                
                
                
                option = OptionBuilder.withArgName("string")
				.hasArgs()
				.withDescription("Path to a file phenotype information from which cell proportions are loaded.\n "
                                               + "Required when you want Cell type specific results in ASreads.")                                                
				.withLongOpt("pheno_file")
				.create('P');
		OPTIONS.addOption(option);
                
                
                /*
                    fully optional arguments
                */
                
                option = OptionBuilder.withArgName("String")
				.hasArgs()
				.withDescription("Location of a file containing a list of snp names (rs123456789 format)\n"
                                               + "Standard setting is none.\n"
                                               + "Used when action is: ASreads.")                                                
				.withLongOpt("snp_list")
				.create("snp_list");
		OPTIONS.addOption(option);
                
                
                
                option = OptionBuilder.withArgName("String")
				.hasArgs()
				.withDescription("Integer specifying how many heterozygotes should present before to run an AS test.\n"
                                               + "Default setting is 1, minimum should be 0 but is not checked.\n"
                                               + "Used when action is: BINOMTEST, BETABINOMTEST, CTSBINOMTEST, CTSBETABINOMTEST.")                                                
				.withLongOpt("minimum_heterozygotes")
				.create("minimum_hets");
		OPTIONS.addOption(option);                
                
                
                option = OptionBuilder.withArgName("String")
				.hasArgs()
				.withDescription("Integer specifying how many reads should be overlapping before to run an AS test.\n"
                                               + "Default setting is 10, minimum should be 0 but is not checked.\n"
                                               + "Used when action is: BINOMTEST, BETABINOMTEST, CTSBINOMTEST, CTSBETABINOMTEST.")                                                
				.withLongOpt("minimum_reads")
				.create("minimum_reads");
		OPTIONS.addOption(option);                
                
                
                

	}
    
    public static void main(String... args) throws Exception{
        
        //Required Arguments
        String outputLocation = new String();
        
        //ASreads specific arguments
        String bamFile = new String();
        String couplingLocation = new String();
        String genotypeLocation = new String();
        String snpsLocation = new String();
        
                
        //BINOMTEST and BETABINOMTEST specific arguments
        String asFile = new String();

        //Cell type specific locations
        String phenoTypeLocation = new String();
        
        try {
            CommandLineParser parser = new PosixParser();
            final CommandLine commandLine = parser.parse(OPTIONS, args, true);
            
            try{
                //Read outputLocation
                
                if(commandLine.hasOption('O')){
                    outputLocation = commandLine.getOptionValue('O');
                } else{
                    throw new ParseException("Required command line input: --output ");
                }
                
                // Optional arguments that are not passed to the Entry constructors
                // But are saved in the GlobalVariables class.
                
                if(commandLine.hasOption("minimum_hets")){
                    GlobalVariables.minHets = Integer.parseInt(commandLine.getOptionValue("minimum_hets"));
                    //Check if this is bigger than 0, otherwise exit 
                    if(GlobalVariables.minHets <= 0){
                        throw new IllegalDataException("Minimum Number of hets cannot be smaller than one for AS testing\n"
                                         + "Exitting");
                    }
                    
                }
                
                if(commandLine.hasOption("minimum_reads")){
                    GlobalVariables.minReads = Integer.parseInt(commandLine.getOptionValue("minimum_reads"));
                    //Check if this is bigger than 0, otherwise exit
                    if(GlobalVariables.minReads <= 0){
                        throw new IllegalDataException("Minimum Number of reads cannot be smaller than one for AS testing\n"
                                         + "Exitting");
                    }
                }
                
                if(commandLine.hasOption('A')){
                    String programAction = commandLine.getOptionValue('A').toUpperCase();
                    
                    if(programAction.equals("ASreads") || programAction.equals("1")){
        
                        //Do the AS determination part of the program
                        
                        //read genotype from options 
                        if(commandLine.hasOption('G')){
                             genotypeLocation = commandLine.getOptionValue('G');
                        } else {
                            throw new ParseException("Required command line input --genotype_location when --action is ASreads");
                        }
                        
                        //Read binomTest arguments
                        if(commandLine.hasOption('C')){
                            couplingLocation = commandLine.getOptionValue('C');
                        } else{
                            throw new ParseException("Required command line input --coupling_file when --action is ASreads");
                        }                        
                         
                        if(commandLine.hasOption('B')){
                            bamFile = commandLine.getOptionValue('B');
                        } else{
                            throw new ParseException("Required command line input --bam_file when --action is ASreads");
                        }         
                        if(commandLine.hasOption("snp_list")){
                            snpsLocation = commandLine.getOptionValue("snp_list");
                        }else{
                            snpsLocation = "";
                        
                        }      
                        
                        
                        
                        /*
                            START reading for AS reads.
                        */
                        readGenoAndAsFromIndividual(bamFile, genotypeLocation, couplingLocation, outputLocation, snpsLocation);
                        
                    
                    }else if(programAction.equals("ASEperSNP") || programAction.equals("2")){
                        

                        if(commandLine.hasOption('L')){
                            asFile = commandLine.getOptionValue('L');
                        } else{
                            throw new ParseException("Required command line input --as_location when --action is ASEperSNP");
                        }
                        if(commandLine.hasOption('P')){
                            phenoTypeLocation = commandLine.getOptionValue('P');
                        } else{
                            phenoTypeLocation = null;
                        }
                        
                       
                        /*
                            START BINOMIAL CELL TYPE SPECIFIC TEST
                        */
                        
                        NonPhasedEntry a =  new NonPhasedEntry(asFile, phenoTypeLocation,  outputLocation);
                        
                    }else if(programAction.equals("ASEperRegion") || programAction.equals("3")){
                        
                        if(commandLine.hasOption('L')){
                            asFile = commandLine.getOptionValue('L');
                        } else{
                            throw new ParseException("Required command line input --as_location when --action is ASEperRegion");
                        }
                        if(commandLine.hasOption('C')){
                            couplingLocation = commandLine.getOptionValue('C');
                        } else{
                            throw new ParseException("Required command line input --coupling_file when --action is ASEperRegion");
                        }           
                        if(commandLine.hasOption('P')){
                            phenoTypeLocation = commandLine.getOptionValue('P');
                        } else{
                            phenoTypeLocation = null;
                        }
                        if(commandLine.hasOption('G')){
                             genotypeLocation = commandLine.getOptionValue('G');
                        } else {
                            throw new ParseException("Required command line input --genotype_Location when --action is ASEperRegion");
                        }
                        
                       PhasedEntry a = new PhasedEntry(asFile, couplingLocation, outputLocation, phenoTypeLocation, genotypeLocation);
                        
                        
                    }else{
                        throw new ParseException("Unable to determine what to do. Please specify a correct value to --Action");
                    }
                }
                
                
                
            }catch (ParseException ex) {
                LOGGER.fatal("Invalid command line arguments");
                LOGGER.fatal(ex.getMessage());
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(" ", OPTIONS);
            
            }
            
        
        } catch (ParseException ex) {
            LOGGER.fatal("Invalid command line arguments: ");
            LOGGER.fatal(ex.getMessage());
            System.err.println();
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(" ", OPTIONS);
        }
    }

}