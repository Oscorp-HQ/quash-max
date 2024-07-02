import { z } from "zod";

export const taskSchema = z.object({
  id: z.string(),
  title: z.string(),
  status: z.string(),
  type: z.string(),
  source: z.string(),
  priority: z.string(),
  reportedByName: z.any(),
  reportedOn: z.string(),
  exportedOn: z.string(),
  exported: z.boolean(),
  description: z.string(),
  listOfMedia: z.any(),
  crashLog: z.any(),
});

export type Task = z.infer<typeof taskSchema>;
