import React from 'react';
import { useDrop } from 'react-dnd';
import { Play, Trash2, Download, RefreshCw } from 'lucide-react';

const CommandForm = ({
  selectedTech,
  formData,
  onFormChange,
  onDrop,
  onGenerateCommand,
  commands,
  onRemoveCommand,
  onClearWorkflow
}) => {
  const [{ isOver }, drop] = useDrop(() => ({
    accept: 'cheatsheet-item',
    drop: (item) => {
      onDrop(item);
    },
    collect: (monitor) => ({
      isOver: monitor.isOver(),
    }),
  }));

  const exportCommands = () => {
    const commandsText = commands.map(cmd => cmd.command).join('\n');
    const blob = new Blob([commandsText], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'devops-commands.txt';
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="bg-white dark:bg-dark-surface rounded-lg shadow-md p-4 h-full transition-colors duration-200 flex flex-col">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-gray-800 dark:text-dark-text">Command Builder</h2>
        {selectedTech && (
          <div className="flex items-center">
            <span className="text-sm font-medium text-gray-700 dark:text-dark-text mr-2">
              Building for: 
            </span>
            <span className="bg-blue-100 dark:bg-dark-primary/30 text-blue-800 dark:text-blue-300 px-3 py-1 rounded-full text-sm font-medium">
              {selectedTech.name}
            </span>
          </div>
        )}
      </div>
      
      {selectedTech ? (
        <div className="flex flex-col flex-grow">
          {/* Drop Zone - Takes most of the space */}
          <div
            ref={drop}
            className={`flex-grow border-2 border-dashed rounded-lg transition-colors flex flex-col ${
              isOver
                ? 'border-blue-400 bg-blue-50 dark:border-blue-400 dark:bg-dark-primary/20'
                : 'border-gray-300 bg-gray-50 dark:border-dark-border dark:bg-dark-secondary'
            }`}
          >
            <div className="flex flex-col h-full">
              <div className="p-4 border-b border-gray-200 dark:border-dark-border">
                <p className="text-gray-600 dark:text-dark-muted text-center">
                  {isOver ? 'Drop here to add to form' : 'Drag cheat sheet items here or fill the form manually'}
                </p>
              </div>
              
              {/* Dynamic Form - Scrollable area */}
              <div className="p-4 overflow-y-auto flex-grow">
                <div className="grid grid-cols-2 gap-4">
                  {Object.keys(formData).length > 0 ? (
                    Object.entries(formData).map(([key, value]) => {
                      // Extract the original key without the counter suffix for display
                      const displayKey = key.includes('_') ? key.split('_')[0] : key;
                      // If this is a duplicate parameter, add an indicator
                      const isDuplicate = key.includes('_');
                      const duplicateNum = isDuplicate ? parseInt(key.split('_')[1]) : null;
                      
                      return (
                        <div key={key} className="bg-white dark:bg-dark-surface p-3 rounded-md shadow-sm border border-gray-200 dark:border-dark-border">
                          <label className="block text-sm font-medium text-gray-700 dark:text-dark-text mb-1">
                            {displayKey} {isDuplicate && <span className="text-xs text-blue-500 dark:text-blue-400">(#{duplicateNum})</span>}
                          </label>
                          <input
                            type="text"
                            value={value}
                            onChange={(e) => onFormChange(key, e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 dark:border-dark-border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-dark-secondary dark:text-dark-text transition-colors duration-200"
                            placeholder={`Enter ${displayKey} value...`}
                          />
                        </div>
                      );
                    })
                  ) : (
                    <div className="col-span-2 text-center py-12">
                      <p className="text-gray-500 dark:text-dark-muted italic">No form fields yet. Drag items from the cheat sheet.</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Command Actions - Fixed at bottom */}
          <div className="mt-4 flex gap-2">
            <button
              onClick={onGenerateCommand}
              disabled={Object.keys(formData).length === 0}
              className="flex-1 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 dark:disabled:bg-gray-700 text-white px-4 py-2 rounded-md flex items-center justify-center gap-2 transition-colors duration-200"
            >
              <Play className="h-4 w-4" />
              Generate Command
            </button>
            <button
              onClick={onClearWorkflow}
              className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-md flex items-center gap-2 transition-colors duration-200"
            >
              <RefreshCw className="h-4 w-4" />
              Clear
            </button>
          </div>
        </div>
      ) : (
        <div className="text-center text-gray-500 dark:text-dark-muted mt-8">
          <p>Select a technology to start building commands</p>
        </div>
      )}

      {/* Command Flow List - Collapsible panel at the bottom */}
      <div className="mt-4 border-t dark:border-dark-border pt-4">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-medium text-gray-700 dark:text-dark-text">Command Flow ({commands.length})</h3>
          {commands.length > 0 && (
            <button
              onClick={exportCommands}
              className="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded-md text-sm flex items-center gap-2 transition-colors duration-200"
            >
              <Download className="h-3 w-3" />
              Export
            </button>
          )}
        </div>
        
        <div className="max-h-48 overflow-y-auto">
          <div className="grid grid-cols-1 gap-3">
            {commands.length > 0 ? (
              commands.map((command) => (
                <div key={command.id} className="p-3 border border-gray-200 dark:border-dark-border rounded-md bg-gray-50 dark:bg-dark-secondary transition-colors duration-200 shadow-sm">
                  <div className="flex justify-between items-start mb-2">
                    <span className="text-xs font-medium px-2 py-0.5 bg-blue-100 dark:bg-dark-primary/30 text-blue-800 dark:text-blue-300 rounded-full">
                      {command.technology}
                    </span>
                    <button
                      onClick={() => onRemoveCommand(command.id)}
                      className="text-red-500 hover:text-red-700 transition-colors duration-200"
                    >
                      <Trash2 className="h-3 w-3" />
                    </button>
                  </div>
                  <div className="bg-white dark:bg-dark-background p-2 rounded border border-gray-200 dark:border-dark-border mt-2 overflow-x-auto">
                    <code className="text-sm font-mono text-gray-800 dark:text-dark-text block whitespace-pre">{command.command}</code>
                  </div>
                  <div className="text-xs text-gray-400 dark:text-dark-muted mt-2 text-right">{command.timestamp}</div>
                </div>
              ))
            ) : (
              <p className="text-gray-500 dark:text-dark-muted text-center italic py-4">No commands generated yet</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CommandForm;