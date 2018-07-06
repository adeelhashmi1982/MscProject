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

    public static void main(String[] args)
    {

      // Let's try some of the OpenNebula Cloud API functionality for VMs.

        String passwd;
        String RequestCPU;
        String username = System.getProperty("user.name");
        passwd = new String(System.console().readPassword("[%s]", "Password:"));

        // First of all, a Client object has to be created.
        // Here the client will try to connect to OpenNebula using the default

        try
        {
        		oneClient = new Client(username + ":" + passwd, "https://csgate1.leeds.ac.uk:2633/RPC2");
RequestCPU="";

////////////////////  to run every five minutes crontab  command : * /5 * * * * Path of the file/Project.java (with arugement)  .... get the path using pwd
if (args.length != 0) {
    //System.out.println("run program with: java myVirtualMachineClass i, where i is 1,2,4,5 or 6 depending on which task you want to run");
    //return;
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

            //System.out.println("5: Enter 5 to continue for more VMs");
            //RequestCPU = new String(System.console().readLine("[%s]", "Enter : "));
            //System.out.println("4: Enter 4 for Exit & optimize");
            RequestCPU= new String(System.console().readLine("[%s]", "Select 1,2,3,5:"));

              //RequestCPU=Integer.parseInt(RequestCPU);
              switch (Integer.parseInt(RequestCPU)) {
                //task one
                //case 1: taskOne(vmTemplate, client, 0); break;
                case 1: VM1(1); break;
                case 2: VM1(2); break;
                case 3: VM1(3); break;
                case 4: CheckCPUThreshold();break;
                case 5: break loop;
                //handle wrong number being entered
                default: System.out.println("task you want to run");

              }
                //RequestCPU= new String(System.console().readLine("[%s]", "Select Template Image [1,2,3]"));

            }

}



            //create the VMSample object to complete the program

      //    VMResource= new ResourceUsage();
      //    VMResource.retrieveInformation( oneClient);
          ///////Host id Returned by our algorithm (Minimum CPU utilization), where the Vm will be deployed or migrated
        //  besthost=VMResource.getHost();

      /*  MinCPUHost=VMResource.getMinCPUHost();
        MaxCPUHost=VMResource.getMaxCPUHost();
        System.out.println( "The HOST ID WITH CPU Utilization < 15% : " + MinCPUHost);
        System.out.println( "The HOST ID WITH CPU Utilization > 50 % : " + MaxCPUHost);*/

//besthost=12;



        }

            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }


//run();
////Creating 8 VirtualMachine each after 30 SECONDS
 /*for (int i=0; i < 1; i++) {


        //SECONDS.sleep(30);
        VM1();

    }*/


//To check the latest status of host after placement and migration
//VMResource.retrieveInformation( oneClient);

// comment or uncomment from the options below to achieve the desired output


/////////////////////CODE TO MIGRATE////////////////////////////////
//migrate();
//MigrationPolicy();
//////////////////////////////////////////////////////////////////////

/////////////////////CODE TO Check Threshold////////////////////////////////
//if (flag=true)
//{
  // CheckCPUThreshold();
//}
//////////////////////////////////////////////////////////////////////
//  GetHostDetails();

/////////////////////CODE TO Delete////////////////////////////////
//cancelandfinalize();
//////////////////////////////////////////////////////////////////////



// cancelandfinalize();
//shutdown();

}



public static void VM1(int RequestCPU)
      {
try {
        // We will try to create a new virtual machine. The first thing we
        // need is an OpenNebula virtual machine template.

        // This VM template is a valid one, but it will probably fail to run
        // if we try to deploy it; the path for the image is unlikely to
        // exist.

        ///////////////////////////////////////////////////////////////////
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

                  // System.out.println("Virtual Machine Template:\n" + vmTemplate);

                  VMResource= new ResourceUsage();
                  VMResource.retrieveInformation( oneClient);
                  ///////Host id Returned by our algorithm (Minimum CPU utilization), where the Vm will be deployed or migrated
                  besthost=VMResource.getHost();


                    System.out.println();
                    System.out.print("Trying to allocate the virtual machine... ");


                    long startTime = System.currentTimeMillis();
                     rc = VirtualMachine.allocate(oneClient, vmTemplate);

                    if( rc.isError() )
                    {
                        System.out.println( "failed!");
                        throw new Exception( rc.getErrorMessage() );
                    }

                    // The response message is the new VM's ID
                    int newVMID = Integer.parseInt(rc.getMessage());
                    System.out.println("ok, ID " + newVMID + ".");

                    // We can create a representation for the new VM, using the returned
                    // VM-ID
                    vm = new VirtualMachine(newVMID, oneClient);

                    // Let's hold the VM, so the scheduler won't try to deploy it
                  ///  System.out.print("Trying to hold the new VM... ");
                  ///  rc = vm.hold();



                  //Deploy VM on Physical host i.e here host id 11 has been defined

                    rc = vm.deploy(besthost);

                    if(rc.isError())
                    {
                        System.out.println("failed!");
                        throw new Exception( rc.getErrorMessage() );
                    }
                    else
                        System.out.println("ok.");

                        ///Checking the status CPUof the VM untill it running
                        while (vm.status() != "runn"){
                        rc=vm.info();

                        }

                    // And now we can request its information.
                  /// rc = vm.info();

                    if(rc.isError())
                        throw new Exception( rc.getErrorMessage() );

                    System.out.println();
                //    System.out.println(

                  //          "This is the information OpenNebula stores for the new VM:");
                  //  System.out.println(rc.getMessage() + "\n");

                    // This VirtualMachine object has some helpers, so we can access its
                    // attributes easily (remember to load the data first using the info
                    // method).
                    System.out.println("The new VM " + vm.getName() + " has status: " + vm.status() + " VM ID: " + vm.getId() );

                    long endTime = System.currentTimeMillis();
                    long elapsed = endTime - startTime;
                    System.out.println("Time Elapsed to deploy... " +  elapsed);

                    // System.out.println("%d%n",elapsed);
                    // And we can also use xpath expressions
                    //System.out.println("VM information :" + vm.info());
//                    System.out.println("The path of the disk is");
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
//System.out.println("HOST ID :" +hostid);
           if (hostid == MaxCPUHost )
           {

                //  System.out.println("sadsadasdasd" + Integer.parseInt(host.xpath("/HOST/VMS/ID")));
               ///////////////////
                   String xml = rc.getMessage();
                   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                   DocumentBuilder builder = factory.newDocumentBuilder();
                   Document doc = builder.parse(new InputSource(new StringReader(xml)));
                   XPathFactory xPathfactory = XPathFactory.newInstance();
                   XPath xpath = xPathfactory.newXPath();
                   NodeList list = (NodeList) xpath.evaluate("/HOST/VMS/ID", doc, XPathConstants.NODESET);

                   for (int i = 0; i < list.getLength(); ++i) {
                       Node node = list.item(i);
                       //Node node = list3.item(i);
                                //   System.out.println(node.getFirstChild().getNodeValue());
                                   vmid=Integer.parseInt(node.getFirstChild().getNodeValue());

                                   System.out.println("My VM ID .........." + vmid);


                                     VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
                                      rc = vmPool.info();

                                   VirtualMachine vm = vmPool.getById(vmid);
                                   if (vm != null)
                                   {
                                      //  System.out.println("~~~~~~~~~~" + vm.getId());
                                    vmCPU = (Double.parseDouble(vm.xpath("/VM_POOL/VM/TEMPLATE/CPU")));
                                  //  vmOwner= vm.xpath("/VM_POOL/VM/UNAME");
                                    //VMCurrentHost = (Integer.parseInt(vm.xpath("/VM/HISTORY_RECORDS/HISTORY/HID") ));

                                        estCPU= vmCPU;
                                        ///////VM id Returned having maximum CPU utilization, This VM will be moigrated to new host
                                      if (estCPU >= minCPU)
                                      {
                                        minCPU = estCPU;
                                        SelectedVMForMigration=Integer.parseInt(vm.getId());

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
            MigrationPolicy();

        }
        else {
        System.out.println("\n Permission Denied.Can not Migrate.You dont own any machine in list.");
        }
      }
      catch (Exception e) {

      System.out.println(e.getMessage());
      }
    }




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
          //System.out.println(rc.getMessage() + "\n");
          hostid=Integer.parseInt(host.xpath("/HOST/ID"));
  //System.out.println("HOST ID :" +hostid);
           if (hostid == MinCPUHost )
           {

                // System.out.println("sadsadasdasd" + Integer.parseInt(host.xpath("/HOST/VMS/ID")));
               ///////////////////
                   String xml = rc.getMessage();
                   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                   DocumentBuilder builder = factory.newDocumentBuilder();
                   Document doc = builder.parse(new InputSource(new StringReader(xml)));
                   XPathFactory xPathfactory = XPathFactory.newInstance();
                   XPath xpath = xPathfactory.newXPath();
                   NodeList list = (NodeList) xpath.evaluate("/HOST/VMS/ID", doc, XPathConstants.NODESET);

                   for (int i = 0; i < list.getLength(); ++i) {
                       Node node = list.item(i);
                       //Node node = list3.item(i);
                                //   System.out.println(node.getFirstChild().getNodeValue());
                                   vmid=Integer.parseInt(node.getFirstChild().getNodeValue());

                                   System.out.println("My VM ID .........." + vmid);

                                     VirtualMachinePool vmPool = new VirtualMachinePool(oneClient);
                                      rc = vmPool.info();
                                      //System.out.println(rc.getMessage() + "\n");
                                    VirtualMachine vm = vmPool.getById(vmid);
                                    //  rc = vmPool.info();
                                   if (vm != null)
                                   {
                                        System.out.println("~~~~~~~~~~" + vm.getId()  + "~~~~" + vmid);
                                        vmCPU = (Double.parseDouble(vm.xpath("/VM_POOL/VM/TEMPLATE/CPU")));
                                    //  VMCurrentHost = (Integer.parseInt(vm.xpath("/VM_POOL/VM/HISTORY_RECORDS/HISTORY/HID")));

                                    //  vmCPUusage = (Double.parseDouble(vm.xpath("/VM/TEMPLATE/CPU") ));
                                    //  VMCurrentHost = (Integer.parseInt(vm.xpath("/VM/HISTORY_RECORDS/HISTORY/HID") ));
                                    //System.out.println("~~~~~vmCPU~~~~~" + vmCPU + "~~~~VMCurrentHost~~~~" + VMCurrentHost );

                                        estCPU= vmCPU;
                                        ///////VM id Returned having maximum CPU utilization, This VM will be moigrated to new host
                                      if (estCPU >= minCPU)
                                      {
                                        minCPU = estCPU;
                                        SelectedVMForMigration=Integer.parseInt(vm.getId());

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
          MigrationPolicy();

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
                      /////Host id Returned by our algorithm, where the VM will be migrated
                                VMResource= new ResourceUsage();
                                VMResource.retrieveInformation( oneClient);

                                MinCPUHost=VMResource.getMinCPUHost();
                                MaxCPUHost=VMResource.getMaxCPUHost();
                                System.out.println( "The HOST ID WITH CPU Utilization < 10 % : " + MinCPUHost);
                                System.out.println( "The HOST ID WITH CPU Utilization > 50 % : " + MaxCPUHost);

                                if (MinCPUHost != 0)
                                {
                                  //  flag=true;
                                    //MigrationPolicy();
                                    System.out.println( "Checking VM List for Min Migration.........." );
                                    GetMinHostDetails();
                                }
                                else {
                                  ///////////set the flag to flase to stop migrationpolicy
                                  //flag=false;
                                }


                                if (MaxCPUHost != 0)
                                {
                                  System.out.println( "Checking VM List for MAX Migration.........." );
                                  //  flag=true;
                                  //flag=false;
                                    //  MigrationPolicy();
                                    GetMaxHostDetails();
                                }
                                else {
                                  ///////////set the flag to flase to stop migrationpolicy
                            //    flag=false;
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


          public static void MigrationPolicy(  )

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

//VMResource.retrieveInformation( oneClient);
                                  /////Host id Returned by our algorithm, where the VM will be migrated
  //                                besthost=VMResource.getHost();
                                //  VMCurrentHost = (Integer.parseInt(vm.xpath("/VM_POOL/VM/HISTORY_RECORDS/HISTORY/HID") ));
                                  VMCurrentHost = (Integer.parseInt(vm.xpath("/VM_POOL/VM/HISTORY_RECORDS/HISTORY/HID")));
                                  System.out.println ("HHOOOOOOOSST" + VMCurrentHost );
                                  System.out.println ("HHOOOOOOOSST22222" + VMResource.getHost2() );

                                  // check the existing host of the VM . if it is same as the suggested for migrattion then migration will not be done

                                if (VMCurrentHost != besthost)
                                {
                                System.out.println("The VM ID : " + SelectedVMForMigration + " , Deployed on Host ID : " + VMCurrentHost + " Will be Migrated to Host ID : " + besthost );
                                     rc = vm.liveMigrate(besthost);

                                     rc=vm.info();
                                     ///Checking the status of the VM untill it running
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


      }


      catch (Exception e) {

      System.out.println(e.getMessage());
      }
}

//////////////////////END NEW VM MIGRATION POLICY/////////////////////////






  /////////////////////////////cancel & finalize///////////////////////////
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
                          ////////Not deleting my 2 virtual machines kept then for future tasks
                        if ( id.equals("11409")  || id.equals("11400") )

                       {
                          System.out.println("i am in");

                        }
                        else
                        {
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
                        ////////Not deleting my 2 virtual machines kept then for future tasks
                        if ( id.equals("11409")  || id.equals("11400") )
                       {
                          System.out.println("i am in");

                        }
                        else
                        {

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

    }
          catch (Exception e) {

          System.out.println(e.getMessage());
          }
      }

      

}
