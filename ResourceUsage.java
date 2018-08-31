/**
	 * Final Code for Resource Usage File
	 * Adeel Ahmed Hashmi
	 * University of Leeds
	 */

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



public class ResourceUsage{

public int minHostID=0;
public int maxHostID=0;
public int MinCPUHostID=0;
public int MaxCPUHostID=0;
public int minHostID2 =0;
public double Psource =0.0;
public double Ptarget =0.0;
//public int Psourceid=0;
//public int Ptargetid=0;



	private OneResponse rc;
	private static DecimalFormat df2 = new DecimalFormat(".##");

	/**
	 * Prints out all the host information available. Along with adding the HOST IDs to a array for future use
	 * @param oneClient
	 */
	public void retrieveInformation(Client oneClient)
	{
		ArrayList <HOSTPERF> arrHost = new ArrayList<HOSTPERF>();


		try
		{

			HostPool pool = new HostPool( oneClient );
			pool.info();

			double cpuUsage, memUsage, diskUsage;
			double minMemUsage=200.0;
			double minCPUUsage=200.0;
			double estPower=0.0;
			int minVMID=0;
			double maxCPUUsage=0.0;
			double estMaxPower=0.0;


			for( Host host: pool)
			{
				rc = host.info();

				cpuUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/CPU_USAGE"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_CPU")))*100;
				memUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MEM_USAGE"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_MEM")))*100;
				diskUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/DISK_USAGE"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_DISK")))*100;
				//diskUsage = (Double.parseDouble(host.xpath("/HOST/HOST_SHARE/getNumVM"))/Double.parseDouble(host.xpath("/HOST/HOST_SHARE/MAX_DISK")))*100;

			 	// get and check the current Host ID of the VM
				System.out.println(".....SOURCE ID......" + Project.getPsourceA() );
				if (Project.getPsourceA() == Integer.parseInt(host.xpath("/HOST/ID")))
				{
					System.out.println(".....SOURCE ID......" + Project.getPsourceA() );
					//Get the CPU usage of the source host
					Psource=cpuUsage;
				}

				// get and check the Target Host ID of the VM
				if (Project.getPtargetA() == Integer.parseInt(host.xpath("/HOST/ID")))
				{
					System.out.println(".....TARGET ID......" + Project.getPtargetA()  );
					//Get the CPU usage of the Target host
					Ptarget=cpuUsage;
				}

				estPower = cpuUsage;

				// Chekcing the Hots with minimum CPU utilization
				if (estPower <= minCPUUsage) {
				minCPUUsage = estPower;
				minHostID= Integer.parseInt(host.xpath("/HOST/ID"));

				// Chekcing the Hots with  CPU utilization less than or equal to 10% i.e our lower bound
						if (minCPUUsage <= 10.00)
						{
							MinCPUHostID=minHostID;
						}

				}


				estMaxPower = cpuUsage;
				// Chekcing the Hots with maximum CPU utilization
				if (estMaxPower > maxCPUUsage) {
				maxCPUUsage = estMaxPower;
				maxHostID= Integer.parseInt(host.xpath("/HOST/ID"));

				// Chekcing the Hots with  CPU utilization greater than 70 % i.e our upper bound
						if (maxCPUUsage > 70.00)
						{
							MaxCPUHostID=maxHostID;
						}
				}

				// Number of VMs running on the Host
				int numVM = Integer.parseInt(host.xpath("/HOST/HOST_SHARE/RUNNING_VMS"));

				arrHost.add(new HOSTPERF(Integer.parseInt(host.xpath("/HOST/ID")), (host.xpath("/HOST/NAME")).toString(), cpuUsage, memUsage, diskUsage, numVM));
			}

			System.out.println("Minimun CPU  usage:...." + minCPUUsage);
			System.out.println("Best host ID:...." + minHostID);

			System.out.println("Physical Hosts with resource usage:....");
			System.out.println("HOSTID\tCPU Usage\tMem Usage\tDisk Usage\tVMs");

			arrHost.sort(Comparator.comparingDouble(HOSTPERF::getCpuUsage));

			boolean flag = true;
			double MinCPU2 = 0.0;
				for(HOSTPERF h: arrHost)
						{
							if (h.HostCpuUsage >= 10.00 && flag == true)
							{
								MinCPU2=h.HostCpuUsage;
								minHostID2=h.HOSTID ;
								flag=false;
							}

							System.out.println(h.HOSTID + "\t" + df2.format(h.HostCpuUsage) +"\t\t" + df2.format(h.HostMemUsage) + "\t\t" + h.HostDiskUsage + "\t\t" + h.NumVM);

						}
								System.out.println();

				}catch(Exception e){
					System.out.println("Error viewing all of the Host info");
					e.printStackTrace();
				}
	}


	/*class of HOST*/
	public class HOSTPERF
	{
		int HOSTID;
		String HOSTNAME;
		double HostCpuUsage;
		double HostMemUsage;
		double HostDiskUsage;
		int NumVM;

		public HOSTPERF(int _hostID, String _hostName, double _cpuUsage, double _memUsage, double _diskUsage, int _numVM)
		{
			HOSTID = _hostID;
			HOSTNAME = _hostName;
			HostCpuUsage = _cpuUsage;
			HostMemUsage = _memUsage;
			HostDiskUsage = _diskUsage;
			NumVM = _numVM;
		}

		public int getID(){
			return HOSTID;
		}
		public String getName(){
			return HOSTNAME;
		}
		public double getCpuUsage(){
			return HostCpuUsage;
		}
		public double getMemUsage(){
			return HostMemUsage;
		}
		public double getDiskUsage(){
			return HostDiskUsage;
		}
		public int getNumVM(){
			return NumVM;
		}
	}

	/**
	 * Logs into the cloud requesting the user's name and password
	 * @param oneClient
	 * @return
	 */
	public Client logIntoCloud() {

		String passwd;
		Client oneClient = null;
		System.out.println("Enter your password: ");
		String username = System.getProperty("user.name");
		passwd = new String(System.console().readPassword("[%s]", "Password:"));
		try
		{
			oneClient = new Client(username + ":" + passwd, "https://csgate1.leeds.ac.uk:2633/RPC2");
			System.out.println("Authentication successful ...");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println("Incorrect Password. Program Closing.");
			System.exit(1);
		}
		return oneClient;
	}

	public static void main(String[] args)
	{

		try
		{
		    //create the VMSample object to complete the program
			ResourceUsage VMSample = new ResourceUsage();
		    //log into the cloud and return the client
			Client oneClient = VMSample.logIntoCloud();
			VMSample.retrieveInformation(oneClient);
        }

		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

	}

	public int getHost() {
		return minHostID; // returns hostid wilth minimium CPU utilization
	}

	public int getHost2() {
		return minHostID2; // reutns the first hostid that is not violating the minimium threshold / lower bound
	}

	public int getMaxCPUHost() {
		return MaxCPUHostID; // returns hostid wilth maximum CPU utilization
	}
	public int getMinCPUHost() {
		return MinCPUHostID; // returns CPU utilization of Host
	}
	public double getPsource() {
			return Psource; // returns CPU utilization of Source Host
	}
	public double getPtarget() {
				return Ptarget; // returns CPU utilization of Target Host
	}
}
