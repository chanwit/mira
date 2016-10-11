package main

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
)

func GetHome() string {
	if runtime.GOOS == "windows" {
		home := os.Getenv("HOMEDRIVE") + os.Getenv("HOMEPATH")
		if home == "" {
			home = os.Getenv("USERPROFILE")
		}
		return home
	}
	return os.Getenv("HOME")
}

func main() {
	home := GetHome()
	miraHome := os.Getenv("MIRA_HOME")
	if miraHome == "" {
		miraHome = filepath.Join(home, ".mira")
	}

	args := []string{
		"-Done-jar.silent=true",
		"-jar",
		filepath.Join(miraHome, "build/libs/mira-standalone.jar"),
	}
	args = append(args, os.Args[1:]...)
	java := "java"
	if runtime.GOOS == "windows" {
		java = java + ".exe"
	}
	executable, err := exec.LookPath(java)
	if err != nil {
		// nothing in the PATH, check JAVA_HOME
		if os.Getenv("JAVA_HOME") != "" {
			executable = filepath.Join(os.Getenv("JAVA_HOME"), "bin", java)
		} else {
			executable = filepath.Join(miraHome, "jdk", "bin", java)
		}
	}

	if len(os.Args) == 1 {
		fmt.Printf("HOME=%s\n", home)
		fmt.Printf("MIRA_HOME=%s\n", miraHome)
		fmt.Printf("JAVA_HOME=%s\n", os.Getenv("JAVA_HOME"))
		fmt.Printf("java: %s\n", executable)
		os.Exit(1)
	}

	cmd := exec.Command(executable, args...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	err = cmd.Run()
	if err != nil {
		os.Exit(-1)
	}
}
