import React, { useState, useEffect } from 'react';
import { DragDropProvider } from './components/DragDropProvider';
import TechnologyPanel from './components/TechnologyPanel';
import CheatSheetPanel from './components/CheatSheetPanel';
import CommandForm from './components/CommandForm';
import WorkflowManager from './components/WorkflowManager';
import { loadWorkflowHistory, saveWorkflowHistory } from './utils/dataManager';
import { defaultTechnologies } from './data/defaultTechnologies';
import { getCheatSheet } from './data/cheatSheets';

function App() {
  const [selectedTechForCheatSheet, setSelectedTechForCheatSheet] = useState(null);
  const [formData, setFormData] = useState({});
  const [commands, setCommands] = useState([]);
  const [workflowHistory, setWorkflowHistory] = useState([]);
  const [darkMode, setDarkMode] = useState(true);

  useEffect(() => {
    // Load workflow history on app start and show the most recent command
    const loadedHistory = loadWorkflowHistory();
    setWorkflowHistory(loadedHistory);
    
    // Load the most recent command if exists
    if (loadedHistory.length > 0) {
      const mostRecent = loadedHistory[loadedHistory.length - 1];
      setCommands(mostRecent.commands);
    }
    
    // Apply dark mode class to document
    if (darkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [darkMode]);

  const handleTechChange = (tech) => {
    // Auto-save current workflow if there are commands
    if (commands.length > 0) {
      const autoWorkflow = {
        id: Date.now(),
        name: `Auto-saved ${selectedTechForCheatSheet?.name || 'Mixed'} - ${new Date().toLocaleString()}`,
        commands: commands,
        createdAt: new Date().toISOString(),
        description: `${commands.length} commands using ${[...new Set(commands.map(c => c.technology))].join(', ')}`
      };

      const updatedHistory = [...workflowHistory, autoWorkflow];
      setWorkflowHistory(updatedHistory);
      saveWorkflowHistory(updatedHistory);
    }

    // Clear current state and set new tech
    setFormData({});
    setCommands([]);
    setSelectedTechForCheatSheet(tech);
  };

  const handleDrop = (draggedItem) => {
    setFormData(prev => {
      // Check if this key already exists in the formData
      if (prev[draggedItem.key]) {
        // For parameters that can have multiple instances (like -e in Docker)
        // Create a unique key by appending a counter
        let counter = 1;
        let newKey = `${draggedItem.key}_${counter}`;
        
        // Find an available key name
        while (prev[newKey]) {
          counter++;
          newKey = `${draggedItem.key}_${counter}`;
        }
        
        return {
          ...prev,
          [newKey]: draggedItem.defaultValue || ''
        };
      } else {
        // If the key doesn't exist yet, add it normally
        return {
          ...prev,
          [draggedItem.key]: draggedItem.defaultValue || ''
        };
      }
    });
  };

  const handleFormChange = (key, value) => {
    setFormData(prev => ({
      ...prev,
      [key]: value
    }));
  };

  const generateCommand = () => {
    if (!selectedTechForCheatSheet || Object.keys(formData).length === 0) return;
    
    let command = selectedTechForCheatSheet.baseCommand || selectedTechForCheatSheet.name;
    
    Object.entries(formData).forEach(([key, value]) => {
      if (value) {
        // Extract the original key without the counter suffix (e.g., '-e_1' -> '-e')
        const originalKey = key.includes('_') ? key.split('_')[0] : key;
        command += ` ${originalKey} ${value}`;
      }
    });
    
    const newCommand = {
      id: Date.now(),
      technology: selectedTechForCheatSheet.name,
      command: command,
      timestamp: new Date().toLocaleString()
    };
    
    setCommands([...commands, newCommand]);
    setFormData({});
  };

  const removeCommand = (commandId) => {
    const updatedCommands = commands.filter(cmd => cmd.id !== commandId);
    setCommands(updatedCommands);
    
    // Also update the workflow history if this command came from a saved workflow
    const updatedHistory = workflowHistory.map(workflow => {
      const updatedWorkflowCommands = workflow.commands.filter(cmd => {
        // Check both original ID and the composite ID format used when loading all workflows
        const compositeId = `${workflow.id}_${cmd.id}_${workflowHistory.indexOf(workflow)}`;
        return cmd.id !== commandId && compositeId !== commandId;
      });
      
      // If commands were removed, update the workflow
      if (updatedWorkflowCommands.length !== workflow.commands.length) {
        return {
          ...workflow,
          commands: updatedWorkflowCommands,
          description: `${updatedWorkflowCommands.length} commands using ${[...new Set(updatedWorkflowCommands.map(c => c.technology))].join(', ')}`
        };
      }
      return workflow;
    });
    
    // Remove empty workflows
    const filteredHistory = updatedHistory.filter(workflow => workflow.commands.length > 0);
    
    if (filteredHistory.length !== workflowHistory.length) {
      setWorkflowHistory(filteredHistory);
      saveWorkflowHistory(filteredHistory);
    }
  };

  const clearWorkflow = () => {
    setCommands([]);
    setFormData({});
  };

  const loadAllWorkflows = () => {
    // Load all commands from all workflows
    const allCommands = workflowHistory.flatMap((workflow, workflowIndex) => 
      workflow.commands.map(cmd => ({
        ...cmd,
        id: `${workflow.id}_${cmd.id}_${workflowIndex}`, // Ensure unique IDs
        workflowName: workflow.name
      }))
    );
    setCommands(allCommands);
  };

  const exportAllWorkflows = () => {
    const exportData = {
      exportDate: new Date().toISOString(),
      totalWorkflows: workflowHistory.length,
      workflows: workflowHistory.map(workflow => ({
        name: workflow.name,
        description: workflow.description,
        createdAt: workflow.createdAt,
        commands: workflow.commands.map(cmd => cmd.command)
      }))
    };
    
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `devops_workflows_export_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  return (
    <DragDropProvider>
      <div className={`min-h-screen ${darkMode ? 'dark' : ''}  transition-colors duration-200`}>
        <div className="bg-gray-100 dark:bg-grid-dark min-h-screen transition-colors duration-200">
          <div className="w-full p-2">
            <header className="flex justify-between items-center mb-6">
              <h1 className="text-2xl font-bold text-gray-800 dark:text-dark-text">DevOps Command Builder</h1>
              <div className="flex items-center gap-4">
                <button
                  onClick={toggleDarkMode}
                  className="p-2 rounded-md bg-gray-200 dark:bg-dark-secondary text-gray-700 dark:text-dark-text hover:bg-gray-300 dark:hover:bg-dark-primary/30 transition-colors duration-200 flex items-center gap-2"
                >
                  {darkMode ? '☀️ Light Mode' : '🌙 Dark Mode'}
                </button>
              </div>
            </header>

            <div className="flex h-[calc(100vh-120px)]">
              {/* Left sidebar - Technology Panel */}
              <div className="w-1/5 mr-4 flex-shrink-0">
                <TechnologyPanel
                  onTechForCheatSheetSelect={handleTechChange}
                  selectedTechForCheatSheet={selectedTechForCheatSheet}
                />
              </div>

              {/* Main content area - Command Form */}
              <div className="w-4/5 flex flex-col">
                <CommandForm
                  selectedTech={selectedTechForCheatSheet}
                  formData={formData}
                  onFormChange={handleFormChange}
                  onDrop={handleDrop}
                  onGenerateCommand={generateCommand}
                  commands={commands}
                  onRemoveCommand={removeCommand}
                  onClearWorkflow={clearWorkflow}
                />
                
                {/* Workflow Manager at the bottom */}
                <div className="mt-4">
                  <WorkflowManager
                    commands={commands}
                    workflowHistory={workflowHistory}
                    setWorkflowHistory={setWorkflowHistory}
                    onLoadAllWorkflows={loadAllWorkflows}
                    onExportAllWorkflows={exportAllWorkflows}
                  />
                </div>
              </div>
            </div>
            
            {/* Draggable Cheat Sheet Panel */}
            {selectedTechForCheatSheet && (
              <CheatSheetPanel
                selectedTech={selectedTechForCheatSheet}
                onClose={() => setSelectedTechForCheatSheet(null)}
              />
            )}

          </div>
        </div>
      </div>
    </DragDropProvider>
  );
}

export default App;