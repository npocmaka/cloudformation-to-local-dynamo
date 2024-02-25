# starts local dynamo from cloudformation template

## requirements

- java 11 (probably will run with java 8 too??)
- maven

## how to use it

### through java
- build it with maven and then include it as dependency in your project.
- import com.npocmaka.local.dynamo.Main and execute the startDynamo("path.to.your.configuration")
- the method will return an object that contains a localDynamo server (which can be stopped), client, connection url and some info about the started local dynamo

### through a command
- check start.sh (execute chmod 777 over the two scripts) and edit the needed variables at the start
- it will start the local dynamo and will create a json file called dynamo.env from which you can get the connection url
- the stop.sh also relies on dynamo.env as it will check the port and pid and will kill the process holding the dynamo
- On windows use stop.bat and start.bat


its all in memory so no need to clean up after stop.


If you intend to edit the code check the lombok project and how to integrate it in your IDE: https://projectlombok.org/



Example usage in java:


	import com.npocmaka.local.dynamo.Main;
	import com.npocmaka.local.dynamo.DynamoEnv;

	public class TestIt {

		public static void main(String[] args) {
			
			DynamoEnv env = Main.startDynamo("./test.yaml");
			
			System.out.println(env.getUri());

		}

	}

