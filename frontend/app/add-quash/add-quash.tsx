import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AppleLogo } from "../lib/icons";
import { AndroidLogo } from "../lib/icons";
import { Code } from "@/components/ui/code-viewer";
import { DisplayKey } from "./display-key";
import BackToDashBoard from "./back-to-dashboard";
import BackToSettings from "./back-to-settings";
import MobileScreen from "../dashboard/components/mobile-screen";

const sdkVersion = "1.1.2";

const AddQuash = ({ appToken }: { appToken: string }) => {
  const jsCodeStep1 = `allprojects {
      repositories {
        ...
        mavenCentral()
      }
    }
    `;

  const jsCodeStep2 = `dependencies {
      implementation("com.quashbugs:sherlock:${sdkVersion}")
    }
    `;

  const jsCodeStep3 = `Quash.initialize(
      this,
      orgUniqueKey = ${appToken},
      readNetworkLogs = true
    )`;

  const jsCodeStep4a = `val client = OkHttpClient.Builder()
    .addInterceptor(Quash.getInstance().networkInterceptor)
    .build()`;

  const iosCode = `
      Hello World
    `;

  return (
    <div className="addquash-container">
      <div className="header-container">
        <BackToSettings />
        <div className="addquash-header">
          <div>
            <h1 className="title ">Add Quash to your application</h1>
            <p className="sub-title ">
              Copy the your unique key and follow the steps to get started.
            </p>
          </div>
          <BackToDashBoard />
        </div>
      </div>

      <div className="body-content">
        <div>
          <p className="key-label">Unique Key</p>
          <DisplayKey appToken={appToken} />
        </div>

        <Tabs defaultValue="android" className="tabs">
          <TabsList>
            <TabsTrigger value="android">
              <AndroidLogo size={16} className="icon" /> Android
            </TabsTrigger>
            <TabsTrigger value="apple" disabled>
              <AppleLogo size={16} className="icon" /> iOS (Coming Soon)
            </TabsTrigger>
          </TabsList>
          <TabsContent value="android">
            <div className="step-container">
              <h2 className="addquash-step">Step 1</h2>
              <p className="addquash-step-description  ">
                Add the plugin as a dependency to your project -
                level.build.gradle file
              </p>
              <Code codeString={jsCodeStep1} />
            </div>

            <div className="step-container">
              <h2 className="addquash-step">Step 2</h2>
              <p className="addquash-step-description ">Add the dependency</p>
              <Code codeString={jsCodeStep2} />
            </div>
            <div className="step-container">
              <h2 className="addquash-step">Step 3</h2>
              <p className="addquash-step-description ">
                With this you can initialize Quash inside the onCreate of the
                Application class of your app
              </p>
              <Code codeString={jsCodeStep3} />
              <p className="addquash-step-description ">
                Include a boolean flag "readNetworkLogs" to determine whether
                network logs should be enabled. Additionally, integrate a
                NetworkInterceptor(step 4) to facilitate the reading of these
                logs.
              </p>
            </div>
            <div className="step-container">
              <h2 className="addquash-step">Step 4</h2>
              <p className="addquash-step-description ">Configure OkHttp:</p>
              <p className="addquash-step-description ">
                Assuming you already have an instance of OkHttpClient, you can
                add an quashinterceptor to it. If you don't have an OkHttpClient
                instance, you need to create one.
              </p>
              <Code codeString={jsCodeStep4a} />
              <p className="addquash-step-description ">
                This example assumes that the
                Quash.getInstance().networkInterceptor returns an instance of
                QuashInterceptor
              </p>
            </div>
          </TabsContent>
          <TabsContent value="apple">
            <div className="step-container">
              <h2 className="addquash-step">Step 1</h2>
              <p className="addquash-step-description ">
                Add the plugin as a dependency to your project -
                level.build.gradle file
              </p>
              <Code codeString={iosCode} />
            </div>

            <div className="step-container">
              <h2 className="addquash-step">Step 2</h2>
              <p className="addquash-step-description ">Add the dependency</p>
              <Code codeString={iosCode} />
            </div>
          </TabsContent>
        </Tabs>

        <p className="footer">
          Not able to move forward?{" "}
          <a href="mailto:hello@quashbugs.com" className="link">
            Contact us
          </a>
        </p>
      </div>
      <MobileScreen />
    </div>
  );
};

export default AddQuash;
