# Quash Quick Start Guide

Welcome to Quash! This guide will walk you through the process of setting up Quash using Docker, whether you're working on your local machine or a hosted VM instance. We've designed this guide to be comprehensive and easy to follow. Let's get started!

## Quick Links
- Host a VM Instance
  - [Google Cloud Platform](#google-cloud-platform-gcp)
  - [Amazon Web Services](#amazon-web-services-aws)
  - [Azure](#microsoft-azure)
- [Running Locally](#running-locally)
- [Prerequisites](#prerequisites)
- [Step-By-Step Guide](#step-by-step-guide)
- [Troubleshooting](#troubleshooting)


## Considerations for Hosted VM Instances

If you plan to run Quash on a virtual machine (VM) hosted in the cloud, here are a few things to consider:

- Choose a VM instance with at least 4GB of memory.
- Configure your firewall rules to allow traffic on ports 8080 and 3000.
-----

You can find detailed instructions on how to create and configure VM instances for different cloud providers here:

### Google Cloud Platform (GCP):

1. **Create a VM instance**: Follow the [GCP guide](https://cloud.google.com/compute/docs/instances/create-start-instance) to create a VM. Choose a machine type with at least 4GB of memory, such as `n1-standard-1`.

2. **Configure firewall rules**: Allow traffic on ports 8080 and 3000 by setting up appropriate firewall rules.

3. **SSH into your VM**: Access your VM through SSH, install the [prerequisites](#prerequisites) and follow the [setup](#step-by-step-guide) steps provided below.
----

### Amazon Web Services (AWS):

1. **Launch an EC2 instance**: Use the [AWS guide](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EC2_GetStarted.html) to launch an EC2 instance. Select an instance type like `t2.medium` which offers 4GB of memory.

2. **Configure security groups**: Ensure your security groups allow inbound traffic on ports 8080 and 3000.

3. **Connect to your EC2 instance**: SSH into your EC2 instance, install the [prerequisites](#prerequisites) and proceed with the [setup](#step-by-step-guide) steps.
----

### Microsoft Azure:

1. **Create a Virtual Machine**: Refer to the [Azure guide](https://docs.microsoft.com/en-us/azure/virtual-machines/windows/quick-create-portal) to create a VM. Opt for a machine size such as `Standard B2ms` with 4GB of memory.

2. **Set up NSGs**: Configure network security groups to permit traffic on ports 8080 and 3000.

3. **Access your VM**: SSH into your Azure VM, install the [prerequisites](#prerequisites) and follow the [setup](#step-by-step-guide) steps.
----

## Running Locally

To run Quash locally, you only need to meet the [prerequisites](#prerequisites) and follow the setup steps below.

## Prerequisites

Before we dive into the setup, let's ensure you have everything you need.

### Docker and Docker Compose

Docker allows you to run applications in isolated containers, while Docker Compose helps you manage multi-container applications. Follow these links to install Docker and Docker Compose on your system:

- [Docker Installation Guide](https://docs.docker.com/get-docker/)
- [Docker Compose Installation Guide](https://docs.docker.com/compose/install/)

### Required Files

Make sure you have the following files downloaded and saved in a folder on your computer:

- [`docker-compose.yml`](https://github.com/Oscorp-HQ/quash-max/blob/main/docker-compose.yml)
- [`setup.sh`](https://github.com/Oscorp-HQ/quash-max/blob/main/setup.sh)

These files contain the instructions and configurations needed to set up Quash.

## Step-by-Step Guide

Now that you have everything ready, let's proceed with the setup.

### Running the Setup Script

1. **Navigate to your working directory**: Open your terminal (or command prompt) and navigate to the folder where you saved the `docker-compose.yml` and `setup.sh` files.

2. **Make the setup script executable**: In the terminal, type the following command to allow the setup script to run:

   ```bash
   chmod +x setup.sh
   ```

3. **Run the setup script**: Start the setup process by running the script with one of the following commands:

   ```bash
   ./setup.sh
   ```

   or if you need administrative permissions:

   ```bash
   sudo bash ./setup.sh
   ```

### Follow the Prompts

As the setup script runs, it will guide you through several steps:

1. **Create containers**: The script will prepare the Docker containers for Quash but will not start them yet.

<div align="center"> <img src="https://github.com/user-attachments/assets/dafb68e6-c3a5-457c-8abd-10c345ab887d" alt="Containers started" width=1000> </div>
<br>

2. **Specify your environment**: You will be asked if you are setting up Quash on a hosted VM instance or locally on your computer. Based on your response, the script will configure the IP addresses for the frontend and backend.
<div align="center"> <img src="https://github.com/user-attachments/assets/9676f186-fbad-4e54-86f0-3b0ec41d7e4c" alt="which environment" width=1000> </div>
<br>

3. **Configure environment files**: The script will copy environment configuration files from the containers to your local directory. You will need to open these files and add your credentials and secrets. This information is crucial for the proper functioning of Quash.
<br>
<div align="center"> <img src="https://github.com/user-attachments/assets/9631d393-ecfb-4340-933d-1501b40ca270" alt="Opening application.properties" width=1000> </div>
<br>
<div align="center"> <img src="https://github.com/user-attachments/assets/0b4deeb3-49f0-4a20-8cb1-b04b4f87f334" alt="nano editor" width=1000> </div>
<br>
<div align="center"> <img src="https://github.com/user-attachments/assets/a40420f9-de71-44b1-a30a-3913ba34fc43" alt="opening .env.local" width=1000> </div>
<br>
<div align="center"> <img src="https://github.com/user-attachments/assets/c841a93f-09de-481b-b0d0-2696f7b1a5cc" alt="nano editor" width=1000> </div>
<br>

4. **Finalize setup**: After you have edited the configuration files, the script will copy them back to the containers and start them.
<br>
<div align="center"> <img src="https://github.com/user-attachments/assets/d5348dd8-4d94-40c5-94ca-385d08f6ae18" alt="Containers started" width=1000> </div>

### Verify the Setup

Once the script has completed its tasks, you should see two running containers:

- **max-frontend**: You can access this at `http://<YOUR_IP>:3000`
- **max-backend**: You can access this at `http://<YOUR_IP>:8080/swagger-ui/index.html`

Replace `<YOUR_IP>` with the actual IP address of your machine or VM instance.

## Troubleshooting

If you run into any issues during the setup process, here are a few tips:

- **Check Docker and Docker Compose**: Ensure both Docker and Docker Compose are installed and running on your system.
- **Verify file locations**: Make sure the `docker-compose.yml` and `setup.sh` files are in the correct directory.
- **Read error messages**: Pay close attention to any error messages and logs provided during the setup. These can give you clues about what might be going wrong.

## Bonus :
If you are utilizing a dynamic IP on a hosted VM instance, please be aware that stopping the instance will halt the container, and upon starting it again, the external IP address of the VM instance will change. However, we have a solution for you. By running the [`restart-max.sh`](https://github.com/Oscorp-HQ/quash-max/blob/main/restart-max.sh) script in your working directory on the VM, the IP address will be updated for the Docker containers, and the containers will be restarted automatically.
**This process is specifically designed for users operating hosted VM instances.**

## Conclusion

Congratulations! You have successfully set up Quash using Docker. If you have any questions or need further assistance, don't hesitate to reach out to the Quash community via [Discord](https://discord.com/invite/Nxbe8F6aqw) or consult the documentation.

Happy bug reporting!
