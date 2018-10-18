# GenTool

GenTool is a tool that helps in manual testing of GenTemplates. With it you can:
* View a list of all discovered GenTemplates, filtered by a glob pattern (e.g. `java*server`)
  * Matching is case-insensitive and unanchored at both ends
* Select any of the matching GenTemplates to execute
* Execute immediately with no params set and a default model used as the primary source (selected based on the GenTemplate's declared primary type)
  * The default models are retrieved from Java resources, and so possibly from within a jar file. For execution, the model is written to a temp file that is automatically deleted when GenTool exits.
  * A temporary directory is allocated for output. The directory is automatically deleted when GenTool exits.
  * Model file and output directory paths are written to stdout, so they can be copied and pasted into an editor or browser or whatever.
* Use advanced operations to construct a more complicated GenTarget for execution:
  * Change the Primary Source
  * Define GenTarget parameter values
  * No named sources or prerequisites at this time.
  
## Launching GenTool

The two easiest ways to run GenTool are:

* Within Eclipse, in a workspace in which the GenFlow projects are open.

  You can set up a "Java Application" launch configuration to run the `GenTool` class, which is located in the `com.reprezen.genflow.tools` package in the `genflow-tools` project.

* From your command line, using a checked out version of the GenFlow repo:

  The trick here is to get the required classpath. The maven dependencies plugin can be used for this, but it won't include the GenFlow project classes, so that needs to be added separately.
  
  Here's what it might look like in a bash shell, assuming you are situated in the root of your GenFlow clone: 
  
  ```
  # this compile everything and copy jar files to your maven cache
  mvn clean install
  # construct classpath required for running GenTool, write it to tmp file
  mvn -f modules/tools dependency:build-classpath -Dmdep.outputFile=/tmp/.gentool-classpath
  # execute GenTool
  java -cp "modules/tools/target/classes:$(</tmp/.gentool-classpath)" com.reprezen.genflow.tools.GenTool 
  ```
  
* From the command line, without a clone of the repo:

  This looks a lot like the case above, but you need retrieve the GenTool artifact first. Assuming your maven cache is in `~/.m2/repository`, the steps will look like this:
  
  ```
  # Replace "<version>" with your desired version
  version=<version>
  # Retrieve the GenTool artifact
  mvn dependency:get -Dartifact=com.reprezen.genflow:genflow-tools:$version
  # shortcut for long paths
  toolLoc=~/.m2/repository/com/reprezen/genflow/genflow-tools/$version/genflow-tools-$version
  # Create classpath using downloaded pom file
  mvn -f  ${toolLoc}.pom dependency:build-classpath -Dmdep.outputFile=/tmp/.gentool-classpath
  # execute GenTool
  java -cp "${toolLoc}.jar:$(</tmp/.gentool-classpath)" com.reprezen.genflow.tools.GenTool
  ```
## General Interaction

GenTool does everything with stdin and stdout, prompting for each required input. There are two modes:
* *String*: Type in any text after the prompt. The text may be validated, and if validation fails you will see the reason, and you will be re-prompted.
* *Choices*: You will see a set of numbered choices, followed by a prompt to make your selection. Type the number corresponding to your selection.

In general, if you type ENTER when prompted, you exit the current interaction, unwinding to the enclosing interaction (i.e. repeating some prior prompt). When you do that in the outermost prompt, GenTool exits.

## Primary Source Specification

When selecting a primary source, you will be prompted with a set of choices. These are:
* First, the default model for your selected GenTemplate
* Next, all files with appropriate file extensions scanned from your current scan directory, if set.
  * The scan directory is initially unset.
  * The scan is recursive, so beware of using scan directories with lots of files!
* Finally, a "Something else..." prompt, where you can enter an arbitrary path.

When you use the "Something else..." prompt, you should enter a file-system path.
* If there is nothing at that path, you will be reprompted.
* If there is a directory at that path, that directory will become your scan directory, and you will drop back to the list of primary source choices, this time including the results of scanning that directory.
* If there is a file at that path, that file will be used as the primary source (even if it has an inappropriate extension), and its containing directory becomes the new scan directory.

## Parameter Specifications

When setting a GenTarget parameter, you must provide a name and a value. For the value, you must indicate your means of specifying the value, and the value itself.

### Name

You will be presented with a list of the parameter names defined by the GenTemplate, followed by a "Something else..." option. If you choose that last option, you will be prompted to enter a name.

### Value
You will be presented with the following choices for specifying your value:

* **Simple String**: You will be prompted for a String value.
* **Empty String**: This choice appears because there's no way to type an empty string using the **Simple String** option; pressing ENTER will cancel the parameter entry.
* **Number**: You will be prompted for a number. If your entry looks like an integer, it will be parsed as an Integer value; otherwise it will be parsed as a Double value.
* **Boolean**: You will be prompted to type either `true` or `false`.
* **JSON**: You will be prompted to type a single-line JSON string. The string is parsed by the Jackson JSON parser, and the `JsonNode` result becomes the parameter value.
** For example, a list of integers might look like `[1,2,3]`
** For example, a JSON object might look like `{"x": 1, "y": 2}`

## Executing the GenTarget

When you type ENTER at the GenTarget specification prompt (rather than choosing to change the primary source or set a parameter), the GenTarget is executed. This is one of the few places where ENTER does not just terminate your current interaction.

You will see any output produced by the GenTemplate or the GenTarget execution logic. You will also see the full paths for the primary source file and for the output directory.
