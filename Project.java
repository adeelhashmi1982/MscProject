 import java.io.*;

 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;

 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;

 import java.util.*;
 import static java.util.concurrent.TimeUnit.SECONDS;
 import java.util.ArrayList;
 import org.opennebula.client.Client;
 import org.opennebula.client.OneResponse;
 import org.opennebula.client.host.Host;
 import org.opennebula.client.host.HostPool;
 import org.opennebula.client.vm.VirtualMachine;
 import org.opennebula.client.vm.VirtualMachinePool;

 import java.text.DecimalFormat;

 import java.io.StringReader;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;


public class Project{
  // First of all, a Client object has to be created.
  static Client oneClient;
  private static DecimalFormat df2 = new DecimalFormat(".##");
  static OneResponse rc;
  static VirtualMachine  vm;
  public static int besthost;
  public static int MinCPUHost=0;
  public static int MaxCPUHost=0;
  public static  ResourceUsage VMResource;
  public static boolean flag = true;
  public static int SelectedVMForMigration=0;
  public static int VMCurrentHost=0;
    public static int AA=100;
  public static int PsourceId=1000;
  public static int PtargetId=2000;

  public static boolean GoodMigrate = true;

    public static void main(String[] args)
    {

        String passwd;
        String RequestCPU;
        String username = System.getProperty("user.name");
        passwd = new String(System.console().readPassword("[%s]", "Password:"));

        // First of all, a Client object has to be created.
        // The client will try to connect to OpenNebula using the following configurtaion

        try
        {
        		oneClient = new Client(username + ":" + passwd, "https://csgate1.leeds.ac.uk:2633/RPC2");
RequestCPU="";

//////////////////// To run every five minutes we will use crontab  command : * /5 * * * * Path of the file/Project.java (with arugement)
// if the project is run with an additional argument : java Project 1     This will be used with the Daemon that will run every 5 mins other wise menu will be listed
if (args.length != 0) {
// Get the latest state of CPU utilization
    CheckCPUThreshold();
}
else
{
  loop: while (RequestCPU != "5")
          {

            System.out.println("Available templatres are :");
            System.out.println("1: Image : Ubuntu , CPU=1 , Memory=1024");
            System.out.println("2: Image : docker , CPU=.1 , Memory=128");
            System.out.println("3: Image : Hadoop , CPU=.5 , Memory=1024");
            System.out.println("4:  optimize");
            System.out.println("5:  Exit");

            RequestCPU= new String(System.console().readLine("[%s]", "Select 1,2,3,4,5:"));

              //RequestCPU=Integer.parseInt(RequestCPU);
              switch (Integer.parseInt(RequestCPU)) {
                case 1: CreateVM(1); break;
                case 2: CreateVM(2); break;
                case 3: CreateVM(3); break;
                case 4: CheckCPUThreshold();break;
                case 5: break loop;
                //handle wrong number being entered
                default: System.out.println("task you want to run");

              }
                //RequestCPU= new String(System.console().readLine("[%s]", "Select Template Image [1,2,3]"));
            }

}

        }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }

//To check the latest status of host after placement and migration
//VMResource.retrieveInformation( oneClient);

}

// This function is used for the creation of VM using three different Templates.The choice of the template will be entered by user
public static void CreateVM(int RequestCPU)
      {
try {

  long VMstartTime = System.currentTimeMillis();
        // We will try to create a new virtual machine. The first thing we
        // need is an OpenNebula virtual machine template.


        /////////////////////////////Three VM Templates Ubuntu, Docker and Hadoop//////////////////////////////////////


        String vmTemplate ="";

        if (RequestCPU==1)
        {
          System.out.println("Creating Vm with option 1");
        	vmTemplate =
        "NAME =MyUB \n"
        + "CPU=\"1\"\n"
        + "SCHED_DS_REQUIREMENTS=\"ID=101\"\n"
        + "NIC=[\n"
        + "\tNETWORK_UNAME=\"oneadmin\",\n"
        + "\tNETWORK=\"vnet1\" ]\n"
        + "LOGO=\"images/logos/linux.png\"\n"
        + "DESCRIPTION=\"Ubuntu, available for testing purposes. In raw format.\"\n"
        + "DISK=[\n"
        + "\tIMAGE_UNAME=\"scsrek\",\n"
        + "\tIMAGE=\"Ubuntu 14.04 LTS x86_64 Base\" ]\n"
        + "SUNSTONE_NETWORK_SELECT=\"YES\"\n"
        + "SUNSTONE_CAPACITY_SELECT=\"YES\"\n"
        + "MEMORY=\"1024\"\n"
        + "HYPERVISOR=\"kvm\"\n"
        + "GRAPHICS=[\n"
        + "\tLISTEN=\"0.0.0.0\",\n"
        + "\tTYPE=\"VNC\" ]\n";
      //  + "CONTEXT=[\n"
      //  + "\t START_SCRIPT=\"apt-get update && apt-get install -y stress && stress --cpu 1 --io 4 --vm 2 --vm-bytes 128M --timeout 30s --verbose \"] \n" ;
      }
      else if (RequestCPU==2) {
          System.out.println("Creating Vm with option 2");
        vmTemplate =
      "NAME =docker \n"
      + "CPU=\"0.1\"\n"
      + "SCHED_DS_REQUIREMENTS=\"ID=101\"\n"
      + "NIC=[\n"
      + "\tNETWORK_UNAME=\"oneadmin\",\n"
      + "\tNETWORK=\"vnet1\" ]\n"
      + "LOGO=\"images/logos/linux.png\"\n"
      + "DESCRIPTION=\"A ttylinux instance with VNC and network context scripts, available for testing purposes. In raw format.\"\n"
      + "DISK=[\n"
      + "\tIMAGE_UNAME=\"oneadmin\",\n"
      + "\tIMAGE=\"ttylinux Base\" ]\n"
      + "SUNSTONE_NETWORK_SELECT=\"YES\"\n"
      + "SUNSTONE_CAPACITY_SELECT=\"YES\"\n"
      + "MEMORY=\"128\"\n"
      + "HYPERVISOR=\"kvm\"\n"
      + "GRAPHICS=[\n"
      + "\tLISTEN=\"0.0.0.0\",\n"
      + "\tTYPE=\"VNC\" ]\n";
      }
      else if (RequestCPU==3) {
          System.out.println("Creating Vm with option 3");
        vmTemplate =
        "NAME =HadoopPseudoCluster-Debian \n"
        + "CPU=\".5\"\n"
        + "SCHED_DS_REQUIREMENTS=\"ID=101\"\n"
        + "NIC=[\n"
        + "\tNETWORK_UNAME=\"oneadmin\",\n"
        + "\tNETWORK=\"vnet1\" ]\n"
        + "LOGO=\"images/logos/linux.png\"\n"
        + "DESCRIPTION=\" This iamge has been created by scsrek, for testing purposes.\"\n"
        + "DISK=[\n"
        + "\tIMAGE_UNAME=\"scsrek\",\n"
        + "\tIMAGE=\"Hadoop Pseudo Cluster - Debian Jessie x86_64 Base\" ]\n"
        + "SUNSTONE_NETWORK_SELECT=\"YES\"\n"
        + "SUNSTONE_CAPACITY_SELECT=\"YES\"\n"
        + "MEMORY=\"1024\"\n"
        + "HYPERVISOR=\"kvm\"\n"
        + "GRAPHICS=[\n"
        + "\tLISTEN=\"0.0.0.0\",\n"
        + "\tTYPE=\"VNC\" ]\n";

      }
      //  + "CONTEXT=[\n"
      //  + "\t START_SCRIPT=\"apt-get update && apt-get install stress && stress --cpu 1 --io 4 --vm 2 --vm-bytes 128M &\"] \n";

                  //To check the latest status of hosts

                  VMResource= new ResourceUsage();
                  VMResource.retrieveInformation( oneClient);

                  ///////besthost is the host id returned by our algorithm (Minimum CPU utilization), where the Vm will be deployed or migrated
                  besthost=VMResource.getHost();

                    System.out.println();
                    System.out.print("Trying to allocate the virtual machine... ");

                    long startTime = System.currentTimeMillis();

                    vmTemplate = vmTemplate + "START=" + VMstartTime;

                      System.out.println(vmTemplate);

                     rc = VirtualMachine.allocate(oneClient, vmTemplate);

                    if( rc.isError() )
                    {
                        System.out.println( "failed!");
                        throw new Exception( rc.getErrorMessage() );
                    }

                    // The response message is the new VM's ID
                    int newVMID = Integer.parseInt(rc.getMessage());
                    System.out.println(" New VM ID " + newVMID + ".");

                    // We can create a representation for the new VM, using the returned
                    // VM-ID
                    vm = new VirtualMachine(newVMID, oneClient);

                  //Deploy VM on Physical host i.e here host id is the most cost effective host returned by our algorithm
                    rc = vm.deploy(besthost);

                    if(rc.isError())
                    {
                        System.out.println("failed!");
                        throw new Exception( rc.getErrorMessage() );
                    }
                    else
                        System.out.println("ok.");

                        ///Checking the status of the VM untill it running
                        while (vm.status() != "runn"){
                        rc=vm.info();
                        }

                    if(rc.isError())
                        throw new Exception( rc.getErrorMessage() );

                    System.out.println();
                    //  System.out.println(
                    //  "This is the information OpenNebula stores for the new VM:");
                    //  System.out.println(rc.getMessage() + "\n");

                    // This VirtualMachine object has some helpers, so we can access its
                    // attributes easily (remember to load the data first using the info
                    // method).
                    System.out.println("The new VM " + vm.getName() + " has status: " + vm.status() + " VM ID: " + vm.getId() );

                    long endTime = System.currentTimeMillis();
                    long elapsed = endTime - startTime;
                    System.out.println("Time Elapsed to deploy the VM : " +  elapsed);

                    // System.out.println("%d%n",elapsed);
                    // And we can also use xpath expressions
                    //System.out.println("VM information :" + vm.info());
                    //System.out.println("The path of the disk is");
                    // System.out.println( "\t" + vm.xpath("template/disk/source") );

                    System.out.println( "\t" + " VM DEPLOYED HAS CPU USAGE : "   + vm.xpath("/VM/TEMPLATE/CPU") );
                    //System.out.println( "\t" + vm.xpath("/VM/UNAME") );
                    System.out.println("\t" + " VM DEPLOYED ON HOSTID : "  + vm.xpath("/VM/HISTORY_RECORDS/HISTORY/HID") );
                    System.out.println("-------------------");
      }
catch (Exception e) {

  System.out.println(e.getMessage());
}
      }



/// This function will list down the details of VM hosted on the host that is violating the upper bound.
    public static void GetMaxHostDetails(  )

    {

      try
  		{
        SelectedVMForMigration=0;

        int hostid = 0;
        int vmid = 0;
        double vmCPU=0.0;

        double estCPU=0.0;
        double minCPU=.1;
        String vmOwner="";

      HostPool pool = new HostPool( oneClient );
      pool.info();
      for( Host host: pool)
			{
				rc = host.info();
				  //System.out.println(rc.getMessage() + "\n");
          hostid=Integer.parseInt(host.xpath("/HOST/ID"));
          //System.out.println("HOST ID~~~~ :" +hostid);
           if (hostid == MaxCPUHost )
           {
                //Checking the list of VMs placed on the Current Host
               //List down the VM Ids on this spesfic hostid
                   String xml = rc.getMessage();
                   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                   DocumentBuilder builder = factory.newDocumentBuilder();
                   Document doc = builder.parse(new InputSource(new StringReader(xml)));
                   XPathFactory xPathfactory = XPathFactory.newInstance();
                   XPath xpath = xPathfactory.newXPath();
                   NodeList list = (NodeList) xpath.evaluate("/HOST/VMS/ID", doc, XPathConstants.NODESET);

                   //get the VM Id of each VM on that host
                   for (int i = 0; i < list.getLength(); ++i) {
                       Node node = list.item(i);

                                   vmid=Integer.parseInt(node.getFirstChild().getNodeValue());
                                   System.out.println("My VM ID .........." + vmid);
                                   VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
                                  rc = vmPool.info();

                                   VirtualMachine vm = vmPool.getById(vmid);
                                   if (vm != null)
                                   {
                                      //  System.out.println("~~~~~~~~~~" + vm.getId());
                                    vmCPU = (Double.parseDouble(vm.xpath("/VM_POOL/VM/TEMPLATE/CPU")));
                                    estCPU= vmCPU;

                                      //VM id Returned having maximum CPU utilization, This VM will be migrated to new host
                                      if (estCPU >= minCPU)
                                      {
                                        minCPU = estCPU;
                                        SelectedVMForMigration=Integer.parseInt(vm.getId());
                                        VMCurrentHost=hostid;
                                        System.out.println("Test CUrrent Host:" + hostid);
					                            //System.out.println("Test Max VM HOST:" + VMCurrentHost);
                                      }

                                  }
                   }

           }
      }

        //System.out.println("CPU OF Selected Vm for Migration :"  + vm.getId() + " IS : " + vmCPU);
        if (SelectedVMForMigration != 0)
        {
            System.out.println("\nTrying to migrate the VMID " + SelectedVMForMigration);
            VMResource.retrieveInformation( oneClient);
            /////Host id Returned by our algorithm, where the VM will be migrated
            besthost=VMResource.getHost();

            // This will initiate the migration process
            MigrateVM();

        }
        else {
        System.out.println("\n Permission Denied.Can not Migrate.You dont own any machine in list.");
        }
      }
      catch (Exception e) {

      System.out.println(e.getMessage());
      }
    }



/// This function will list down the details of VM hosted on the host that is violating the lower bound.
    public static void GetMinHostDetails(  )

    {

      try
      {
        SelectedVMForMigration=0;
        int hostid = 0;
        int vmid = 0;
        double vmCPU=0.0;

        double estCPU=0.1;
        double minCPU=0.1;


      HostPool pool = new HostPool( oneClient );
      pool.info();
      for( Host host: pool)
      {
        rc = host.info();
          // System.out.println(rc.getMessage() + "\n");
          hostid=Integer.parseInt(host.xpath("/HOST/ID"));
          // System.out.println("HOST ID~~~ :" + hostid);
           if (hostid == MinCPUHost )
           {
              //Checking the list of VMs placed on the Current Host
              //List down the VM Ids on this spesfic hostid
                   String xml = rc.getMessage();
                   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                   DocumentBuilder builder = factory.newDocumentBuilder();
                   Document doc = builder.parse(new InputSource(new StringReader(xml)));
                   XPathFactory xPathfactory = XPathFactory.newInstance();
                   XPath xpath = xPathfactory.newXPath();
                   NodeList list = (NodeList) xpath.evaluate("/HOST/VMS/ID", doc, XPathConstants.NODESET);

              //Get the VM Id of each VM on that host
                   for (int i = 0; i < list.getLength(); ++i) {
                       Node node = list.item(i);

                                   vmid=Integer.parseInt(node.getFirstChild().getNodeValue());
                                   System.out.println("My VM ID .........." + vmid);
                                     VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
                                      rc = vmPool.info();
                                      //System.out.println(rc.getMessage() + "\n");
                                    VirtualMachine vm = vmPool.getById(vmid);
                                    //  rc = vmPool.info();
                                   if (vm != null)
                                   {
                                       // System.out.println("~~~~~~~~~~" + vm.getId()  + "~~~~" + vmid);
                                        vmCPU = (Double.parseDouble(vm.xpath("/VM_POOL/VM/TEMPLATE/CPU")));
                                        estCPU= vmCPU;

                                      //VM id Returned having maximum CPU utilization, This VM will be migrated to new host
                                      if (estCPU >= minCPU)
                                      {
                                        minCPU = estCPU;
                                        SelectedVMForMigration=Integer.parseInt(vm.getId());
                                        VMCurrentHost=hostid;
                                         System.out.println("Test CUrrent Host:" + hostid);
                              				//	System.out.println("Test CUrrent Host:" + VMCurrentHost);
                                      }

                                  }
                   }

           }
      }
      if (SelectedVMForMigration != 0)
      {
          System.out.println("\nTrying to migrate the VMID " + SelectedVMForMigration);
          VMResource.retrieveInformation( oneClient);
          /////Host id Returned by our algorithm, where the VM will be migrated
          besthost=VMResource.getHost2();
          //This will initiate the migration process
          MigrateVM();

      }
      else{
        System.out.println("\n Permission Denied.Can not Migrate.You dont own any machine in list.");
      }
      }
      catch (Exception e) {

      System.out.println(e.getMessage());
      }
    }


    //////////////////////CHECK CPU THRESHOLD FOR MIGRATION/////////////////////////

    public static void CheckCPUThreshold(  )

    {


    try{
                                VMResource= new ResourceUsage();
                                VMResource.retrieveInformation( oneClient);

                                ///// MinCPUHost and MaxCPUHost will have the CPU lower bound and upper bound details i.e the host with max and min CPU utilization
                                MinCPUHost=VMResource.getMinCPUHost(); // will return the hostid with min CPU utilization
                                MaxCPUHost=VMResource.getMaxCPUHost(); // will return the hostid with max CPU utilization
                                System.out.println( "The Host ID with CPU Utilization < 10 % : " + MinCPUHost);
                                System.out.println( "The Host ID with CPU Utilization > 70 % : " + MaxCPUHost);

                                if (MinCPUHost != 0)
                                {

                                    System.out.println( "Checking VM List for Min Migration.........." );
                                  //    ManageJob();
                                    GetMinHostDetails();  // This will return the list of VMs along with details , placed on the host with minimum CPU utilization.
                                }
                                else {
                                  // set the flag to flase to stop MigrateVM
                                  // flag=false;
                                }


                                if (MaxCPUHost != 0)
                                {
                                  System.out.println( "Checking VM List for MAX Migration.........." );
                                    GetMaxHostDetails(); // This will return the list of VMs along with details , placed on the host with minimum CPU utilization.
                                }
                                else {
                                  // set the flag to flase to stop MigrateVM
                                  // flag=false;
                              }

                                 // This is all the information you can get from the OneResponse:
                               System.out.println("\tOpenNebula response");
                               System.out.println("\t Error: " + rc.isError());
                            //   System.out.println("\t Msg: " + rc.getMessage());
                               System.out.println("\t ErrMsg: " + rc.getErrorMessage());


    }
        catch (Exception e) {

        System.out.println(e.getMessage());
        }
    }

    //////////////////////////END CHECK CPU THRESHOLD FOR MIGRATION//////////////////





    /////////////////////////////NEW VM MIGRATION POLICY///////////////////////////

    // This function will perform the migration of the VMS
          public static void MigrateVM(  )

          {

        try{

          VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
           rc = vmPool.info();
           VirtualMachine vm = vmPool.getById(SelectedVMForMigration);
           rc = vmPool.info();
           String id = vm.getId();
           String name = vm.getName();

           String enab = vm.xpath("enabled");
           System.out.println("ID : NAME : " + id+"\t\t"+name+"\t\t"+enab);

                                  System.out.println ("Best Host > 10% " + VMResource.getHost2() );

                                  //  ManageJob() will check the status of the job . if the job has sufficient time left to recover the the migration cost . then migration will be done
                                    ManageJob();
                                    if (GoodMigrate== true){

                                      // check if the existing host of the VM is same as target host . if both are same than migration will not be done

                                                  if (VMCurrentHost != besthost)
                                                  {
                                                      System.out.println("The VM ID : " + SelectedVMForMigration + " , Deployed on Host ID : " + VMCurrentHost + " Will be Migrated to Host ID : " + besthost );

                                                      //This command will perform the livemigration of the VM to target host i.e besthost
                                                      rc = vm.liveMigrate(besthost);
                                                      rc=vm.info();
                                                      //Checking the status of the VM untill its running
                                                        while (vm.status() != "runn")
                                                        {
                                                          rc=vm.info();
                                                        }
                                                        System.out.println("Migration Completed..... " + vm.status());

                                                      // This is all the information you can get from the OneResponse:
                                                       System.out.println("\tOpenNebula response");
                                                       System.out.println("\t Error: " + rc.isError());
                                                       //System.out.println("\t Msg: " + rc.getMessage());
                                                       System.out.println("\t ErrMsg: " + rc.getErrorMessage());
                                                    }
                                                    else
                                                    {
                                                //No need to migrate to same host
                                                    }
                                        GoodMigrate=false;
                                      }
       }


      catch (Exception e) {

      System.out.println(e.getMessage());
      }
}

//////////////////////END NEW VM MIGRATION POLICY/////////////////////////



/////////////////////////////Manage JOBs///////////////////////////

////The funtion getPsourceA returns the VM Source host ID
public static double getPsourceA() {
  PsourceId=VMCurrentHost;
  return PsourceId;
}


////The funtion getPsourceA returns the VM Target host ID
public static double getPtargetA() {
  PtargetId=besthost;
  return PtargetId;
}



/////////////////////The ManageJob() will do the migration cost estimation and evaluate if the migration is going to be cost effective or not

    public static void ManageJob(  )

    {


  try{

      /////////// if VM remaining time is greater than the calcutated time then we will do the migration else dont i.e   (Roffset > Toff ) .
      VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
       rc = vmPool.info();
       VirtualMachine vm = vmPool.getById(SelectedVMForMigration);
       rc = vmPool.info();
       String enab = vm.xpath("enabled");
      //long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
      //long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

                          PsourceId=VMCurrentHost;
                          PtargetId=besthost;

                            /// get the VM source Host ID
                          double Psource  =VMResource.getPsource();
                          System.out.println(" Psource ..... " + PsourceId + "...."+ Psource);

                          /// get the VM Target Host ID
                          double Ptarget=VMResource.getPtarget();
                          System.out.println(" Ptarget ..... " + PtargetId  +"...."+ Ptarget);

                            long Rtotal=20 * 60000;  //total running time of 20 mins in milisecs

                            long Start= (Long.parseLong(vm.xpath("/VM_POOL/VM/USER_TEMPLATE/START")));
                            System.out.println(" CURRENT  VM TIME ..... " + vm.xpath("/VM_POOL/VM/USER_TEMPLATE/START"));
                            long currenttime = System.currentTimeMillis();
                            /// The time VM spent of the SOurce Machine before successful migration to Target
                            long Rpast= currenttime - Start;

                            // The Power of the Source Host (Psource ) and Target Host (Ptarget)
                            double Dx=Psource - Ptarget;
                            // The total cost of migration
                            double Costmig= 6 * Psource; // Migtime * Psource
                            // Time at which the VM has recvoered its migration cost and after that it will be saving power
                            double Toff= Costmig/Dx;

                            //Roffset=Rpast+tmig+toff
                           double Roffset= Rpast + 60000 + Toff;
                            System.out.println ("Rpast time ........." + Rpast);
                              System.out.println ("Roffset time ........." + Roffset);
                                System.out.println ("Toff time ........." + Toff);
/// if Roffset > Toff then it means our migration will be cost effective
if (Roffset >= Toff)
{
  //good to migrate
  GoodMigrate=true;

}
else
{
  //Not good to migrate
    GoodMigrate=false;
}

      }

        catch (Exception e) {

        System.out.println(e.getMessage());
        }
    }




  ///////////////////////////// Function that will perform the cancel & finalize opertaion on the VM ///////////////////////////
      public static void cancelandfinalize(  )

      {

    try{

      VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
      rc = vmPool.info();
                  //  System.out.println("--------------------------------------------");
                    System.out.println("Number of VMs: " + vmPool.getLength());
                    System.out.println("User ID\t\tName\t\tEnabled");
                    // You can use the for-each loops with the OpenNebula pools
                    for( VirtualMachine vm : vmPool )
                    {
                        String id = vm.getId();
                        String name = vm.getName();
                        String enab = vm.xpath("enabled");
                        System.out.println(id+"\t\t"+name+"\t\t"+enab);


                      ///give the delay of 30Secs between each delete
                      SECONDS.sleep(30);
                      long startTimeDel = System.currentTimeMillis();


                              //  rc = vm.cancel();
                                System.out.println("\nTrying to cancel the VM " + vm.getId());

                              //Command to Delete the VM
                               rc = vm.finalizeVM();

                              ///Checking the status of the VM untill we recieve done
                               while (vm.status() != "done")
                               {
                               rc=vm.info();
                               }

                               long endTimeDel = System.currentTimeMillis();
                               long delete = endTimeDel - startTimeDel;
                               System.out.println("Time Elapsed to delete... " +  delete);
                               // This is all the information you can get from the OneResponse:
                             System.out.println("\tOpenNebula response");
                             System.out.println("\t Error: " + rc.isError());
                             System.out.println("\t Msg: " + rc.getMessage());
                             System.out.println("\t ErrMsg: " + rc.getErrorMessage());

                    }

}
            catch (Exception e) {

              System.out.println(e.getMessage());
            }
      }




  /////////////////////////////Shutdown///////////////////////////

      public static void shutdown(  )

      {


    try{

      VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
      rc = vmPool.info();
                  //  System.out.println("--------------------------------------------");
                    System.out.println("Number of VMs: " + vmPool.getLength());
                    System.out.println("User ID\t\tName\t\tEnabled");
                    // You can use the for-each loops with the OpenNebula pools
                    for( VirtualMachine vm : vmPool )
                    {
                        String id = vm.getId();
                        String name = vm.getName();
                        String enab = vm.xpath("enabled");
                        System.out.println(id+"\t\t"+name+"\t\t"+enab);

            //////////////////give the delay of 30Secs between each shutdown
                      SECONDS.sleep(30);
                      long startTimeshut = System.currentTimeMillis();


                              //  rc = vm.cancel();
                                System.out.println("\nTrying to Shutdown the VM " + vm.getId());
                              // rc = vm.finalizeVM();
                              ////command to shutdown the VM
                              rc=vm.shutdown();

                              ///Checking the status of the VM untill we recieve done

                              while (vm.status() != "done")
                              {
                              rc=vm.info();
                              }

                               long endTimeshut = System.currentTimeMillis();
                               long shut = endTimeshut - startTimeshut;
                               System.out.println("Time Elapsed to shutdown... " +  shut);
                               // This is all the information you can get from the OneResponse:
                             System.out.println("\tOpenNebula response");
                             System.out.println("\t Error: " + rc.isError());
                             System.out.println("\t Msg: " + rc.getMessage());
                             System.out.println("\t ErrMsg: " + rc.getErrorMessage());

                    }

    }
          catch (Exception e) {

          System.out.println(e.getMessage());
          }
      }



}
