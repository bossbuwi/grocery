import { spawn } from "node:child_process";
import { existsSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import process from "node:process";

const projectDirectory = dirname(fileURLToPath(import.meta.url));
const envFile = resolve(projectDirectory, ".env.local");

if (existsSync(envFile)) {
  process.loadEnvFile(envFile);
}

const child = spawn(
  "npx",
  [
    "-y",
    "@bytebase/dbhub@latest",
    "--transport",
    "stdio",
    "--config",
    resolve(projectDirectory, "dbhub.toml"),
  ],
  {
    cwd: projectDirectory,
    env: process.env,
    shell: process.platform === "win32",
    stdio: "inherit",
  },
);

child.on("error", (error) => {
  console.error(`Failed to start DBHub: ${error.message}`);
  process.exitCode = 1;
});

child.on("exit", (code) => {
  process.exitCode = code ?? 1;
});
