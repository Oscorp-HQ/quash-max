import React, { Dispatch, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import SpinLoader from "@/components/ui/spinner";
import { GetReportsById } from "@/app/apis/dashboardapis";
import { useToast } from "@/components/ui/use-toast";
import { useDispatch, useSelector } from "react-redux";
import {
  ReduxState,
  fetchGenerateGifSlice,
  fetchGifSessionAsync,
  selectGeneratedGif,
} from "@/lib/redux";
import { MediaData, Report } from "@/app/types/dashboard-types";
import {
  ThreadAttachment,
  ThreadAttachmentLocal,
} from "@/app/types/comment-types";

/**
 * Function that handles the generation of a GIF session based on the provided reportId.
 * It triggers the generation process, updates the state with the generated GIF data,
 * and handles error scenarios. It also allows refreshing the report and fetching the session.
 *
 * @param reportId - The ID of the report for which the GIF session is being generated.
 * @param setMediaArray - Function to set the media array state.
 * @param mediaArray - The current media array state.
 * @param setGifCreated - Function to set the GIF creation state.
 * @param bugListOriginal - The original list of bugs.
 * @param setBugListOriginal - Function to set the original bug list state.
 * @param gifCreated - Boolean indicating if the GIF has been created.
 * @param gifStatus - The status of the GIF generation process.
 */

interface GifData {
  data: MediaData;
  message: string;
  success: boolean;
}

const GenerateSessionButton = ({
  reportId,
  setMediaArray,
  mediaArray,
  setGifCreated,
  bugListOriginal,
  setBugListOriginal,
  gifCreated,
  gifStatus,
  setSelectedRowValues,
}: {
  reportId: string;
  setMediaArray: React.Dispatch<
    React.SetStateAction<
      (ThreadAttachment | ThreadAttachmentLocal | MediaData)[]
    >
  >;
  mediaArray: (ThreadAttachment | ThreadAttachmentLocal | MediaData)[];
  setGifCreated: React.Dispatch<React.SetStateAction<boolean | null>>;
  bugListOriginal: Report[];
  setBugListOriginal: React.Dispatch<React.SetStateAction<Report[]>>;
  gifCreated: boolean | null;
  gifStatus: string | null;
  setSelectedRowValues: React.Dispatch<React.SetStateAction<Report[]>>;
}) => {
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();
  const dispatch: Dispatch<any> = useDispatch();

  const generatedGif: {
    loading: string;
    gifData: GifData;
  } = useSelector((state: ReduxState) => selectGeneratedGif(state, reportId));

  useEffect(() => {
    if (generatedGif?.loading === "success") {
      if (!gifCreated) {
        generateGif(generatedGif?.gifData);
      }
      setLoading(false);
    } else if (generatedGif?.loading === "failed") {
      toast({
        description: "Failed to fetch generatedGif",
        typeof: "error",
      });
      setLoading(false);
    } else if (generatedGif?.loading === "idle") {
      setLoading(false);
    } else if (generatedGif?.loading === "pending") {
      setLoading(true);
    } else {
      setLoading(false);
    }
  }, [generatedGif, gifCreated]);

  const generateGif = async (responseData: GifData) => {
    setLoading(true);
    try {
      const { data, message, success } = await responseData;
      if (success) {
        setMediaArray([...mediaArray, data]);
        setGifCreated(true);
        let temp: Report[] = [];
        temp = bugListOriginal.map((bug: Report) => {
          if (reportId === bug.id) {
            return {
              ...bug,
              gifStatus: "COMPLETED",
              listOfMedia:
                bug?.listOfMedia && bug?.listOfMedia.length !== null
                  ? [...bug.listOfMedia, data]
                  : [data],
            };
          } else {
            return bug;
          }
        });
        setBugListOriginal([...temp]);
      } else {
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  const refreshReport = async () => {
    setLoading(true);
    try {
      const { data, message, success } = await GetReportsById(reportId);
      if (success) {
        let temp: Report[] = [];
        temp = bugListOriginal.map((bug: Report) => {
          if (reportId === bug.id) {
            return {
              ...bug,
              listOfMedia:
                data.listOfMedia && data.listOfMedia.length > 0
                  ? [...data.listOfMedia]
                  : null,
              gifStatus: data.gifStatus,
            };
          } else {
            return bug;
          }
        });
        setBugListOriginal([...temp]);
        setSelectedRowValues({ ...data });
      } else {
        toast({
          description: message
            ? message
            : "Something went wrong. Please try again.",
          typeof: "error",
        });
      }
    } catch (error: unknown) {
      console.log(error);
      toast({
        description: "Something wen’t wrong, please try again.",
        typeof: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleFetchSession = async () => {
    if (
      generatedGif &&
      generatedGif.gifData &&
      generatedGif.loading === "success"
    ) {
      generateGif(generatedGif?.gifData);
    } else {
      dispatch(
        fetchGenerateGifSlice.actions.updateGenerateGifState({
          id: reportId,
        }),
      );
      dispatch(fetchGifSessionAsync(reportId));
    }
  };

  const handleClick = () => {
    if (gifStatus === "PROCESSING") {
      refreshReport();
    } else {
      handleFetchSession();
    }
  };

  return (
    <div className="side-pane-report-media">
      <div style={{ textAlign: "center", marginTop: "10px" }}>
        <Button
          className="generate-gif-button w-[166px]"
          onClick={handleClick}
          disabled={loading}
        >
          {loading ? (
            <SpinLoader />
          ) : gifStatus === "PROCESSING" ? (
            "Refresh Report"
          ) : (
            "Generate Session"
          )}
        </Button>
      </div>
    </div>
  );
};

export default GenerateSessionButton;
