/*
* Form1.cs
* -------------------------------
* TestRunnerUI - Windows Forms Application
* 
* This application allows the user to run an external test executable (.exe)
* and view its output within a GUI. The main features include:
* 
* - Selecting a test executable using a file dialog
* - Running the selected executable and capturing its standard output and error
* - Displaying test results in a RichTextBox
* - Filtering the output to show all results, only passed tests, or only failed tests
* - Updating the status label to reflect current actions and results
* 
* Components:
* - btnRunTests: Triggers the file dialog and starts test execution
* - cmbFilter: Dropdown to filter output display (All, Passed, Failed)
* - richTextBox1: Displays the filtered test output
* - lblStatus: Shows the current status of the operation (e.g., running, complete, error)
*/

using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Windows.Forms;

namespace TestRunnerUI
{
    public partial class Form1 : Form
    {
        // Store all test outputs for filtering
        private string fullTestOutput;

        public Form1()
        {
            InitializeComponent();

            cmbFilter.Items.AddRange(new string[]
            {
                "All Output",
                "Only Passed Tests",
                "Only Failed Tests"
            });

            cmbFilter.SelectedIndex = 0; // Default selection
            cmbFilter.SelectedIndexChanged += cmbFilter_SelectedIndexChanged;
        }

        // File selection dialog for test executable 
        private void btnRunTests_Click(object sender, EventArgs e)
        {
            richTextBox1.Clear();
            lblStatus.Text = "Status: Waiting for File...";

            using (OpenFileDialog ofd = new OpenFileDialog())
            {
                ofd.Filter = "Executable Files (*.exe)|*.exe";
                ofd.Title = "Select Test Executable";


                if (ofd.ShowDialog() == DialogResult.OK)
                {
                    string testExePath = ofd.FileName;
                    RunTestExecutable(testExePath);
                }
                else
                {
                    lblStatus.Text = "Status: Test Cancelled";
                }
            }
        }

        // Executes the selected test executable and captures output
        private void RunTestExecutable(string testExePath)
        {
            lblStatus.Text = "Status: Running...";

            // Ensure the file exists before running 
            if (!File.Exists(testExePath))
            {
                MessageBox.Show("Test executable not found:\n" + testExePath, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                lblStatus.Text = "Status: Executable Not Found";
                return;
            }

            try
            {
                ProcessStartInfo psi = new ProcessStartInfo
                {
                    FileName = testExePath,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                using (Process proc = new Process { StartInfo = psi })
                {
                    proc.Start();
                    string output = proc.StandardOutput.ReadToEnd();
                    string error = proc.StandardError.ReadToEnd();
                    proc.WaitForExit();

                    fullTestOutput = output;
                    DisplayFilteredOutput(); // Show initial results based on current filter

                    if (!string.IsNullOrWhiteSpace(error))
                    {
                        richTextBox1.AppendText("\n--- Errors ---\n" + error);
                        lblStatus.Text = "Status: Completed with Errors";
                    }
                    else
                    {
                        lblStatus.Text = "Status: Test Run Complete";
                    }
                }
            }
            catch (Exception ex)
            {
                // Catch unexpected issues 
                MessageBox.Show("An error occurred:\n" + ex.Message, "Exception", MessageBoxButtons.OK, MessageBoxIcon.Error);
                lblStatus.Text = "Status: Error";
            }
        }

        // Displays output based on selected filter and applies color + bold formatting
        private void DisplayFilteredOutput()
        {
            if (string.IsNullOrEmpty(fullTestOutput)) return;

            string filter = cmbFilter.SelectedItem?.ToString() ?? "All Output";
            string[] lines = fullTestOutput.Split(new[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries);

            richTextBox1.Clear();

            foreach (var line in lines)
            {
                bool includeLine = false;

                // Decide whether to include this line based on filter selection
                if (filter == "Only Passed Tests" && line.Contains("PASSED"))
                {
                    includeLine = true;
                }
                else if (filter == "Only Failed Tests" && line.Contains("FAILED"))
                {
                    includeLine = true;
                }
                else if (filter == "All Output")
                {
                    includeLine = true;
                }

                if (includeLine)
                {
                    // Set formatting based on result type
                    if (line.Contains("PASSED"))
                    {
                        richTextBox1.SelectionColor = Color.Green;
                        richTextBox1.SelectionFont = new Font(richTextBox1.Font, FontStyle.Bold);
                    }
                    else if (line.Contains("FAILED"))
                    {
                        richTextBox1.SelectionColor = Color.Red;
                        richTextBox1.SelectionFont = new Font(richTextBox1.Font, FontStyle.Bold);
                    }
                    else
                    {
                        richTextBox1.SelectionColor = Color.Black;
                        richTextBox1.SelectionFont = new Font(richTextBox1.Font, FontStyle.Regular);
                    }

                    // Append line with current formatting
                    richTextBox1.AppendText(line + Environment.NewLine);
                }
            }

            // Reset formatting
            richTextBox1.SelectionColor = Color.Black;
            richTextBox1.SelectionFont = new Font(richTextBox1.Font, FontStyle.Regular);
        }

        // Refresh output when filter is changed
        private void cmbFilter_SelectedIndexChanged(object sender, EventArgs e)
        {
            DisplayFilteredOutput();
        }
    }
}
