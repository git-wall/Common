import React from 'react';
import { Eye, Download } from 'lucide-react';

const WorkflowManager = ({ 
  commands, 
  workflowHistory, 
  setWorkflowHistory, 
  onLoadAllWorkflows, 
  onExportAllWorkflows 
}) => {

  return (
    <div className="bg-white dark:bg-dark-surface rounded-lg shadow-md p-4 transition-colors duration-200">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-gray-800 dark:text-dark-text">
          Workflow Manager ({workflowHistory.length} saved workflows)
        </h2>
        <div className="flex gap-2">
          <button
            onClick={onLoadAllWorkflows}
            disabled={workflowHistory.length === 0}
            className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-300 dark:disabled:bg-gray-700 text-white px-4 py-2 rounded-md flex items-center gap-2 transition-colors duration-200"
          >
            <Eye className="h-4 w-4" />
            Show All Commands
          </button>
          <button
            onClick={onExportAllWorkflows}
            disabled={workflowHistory.length === 0}
            className="bg-green-500 hover:bg-green-600 disabled:bg-gray-300 dark:disabled:bg-gray-700 text-white px-4 py-2 rounded-md flex items-center gap-2 transition-colors duration-200"
          >
            <Download className="h-4 w-4" />
            Export All
          </button>
        </div>
      </div>

      {/* Recent Workflows Summary */}
      <div className="space-y-2">
        <h3 className="text-sm font-medium text-gray-600 dark:text-dark-muted">
          Recent Auto-Saved Workflows:
        </h3>
        
        {workflowHistory.length > 0 ? (
          <div className="max-h-32 overflow-y-auto space-y-2">
            {workflowHistory.slice(-5).reverse().map((workflow) => (
              <div key={workflow.id} className="p-2 bg-gray-50 dark:bg-dark-secondary rounded border border-gray-200 dark:border-dark-border">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-800 dark:text-dark-text truncate">
                      {workflow.name}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-dark-muted">
                      {workflow.description}
                    </p>
                    <p className="text-xs text-gray-400 dark:text-gray-500">
                      {new Date(workflow.createdAt).toLocaleString()}
                    </p>
                  </div>
                  <span className="bg-blue-100 dark:bg-dark-primary/30 text-blue-800 dark:text-blue-300 px-2 py-0.5 rounded-full text-xs">
                    {workflow.commands.length} cmds
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center text-gray-500 dark:text-dark-muted py-4">
            <p className="text-sm">No workflows saved yet</p>
            <p className="text-xs">Commands will be auto-saved when you switch technologies</p>
          </div>
        )}
      </div>

      {/* Current Session Info */}
      {commands.length > 0 && (
        <div className="mt-4 pt-4 border-t border-gray-200 dark:border-dark-border">
          <h3 className="text-sm font-medium text-gray-600 dark:text-dark-muted mb-2">
            Current Session:
          </h3>
          <div className="bg-blue-50 dark:bg-dark-primary/20 p-3 rounded border border-blue-200 dark:border-blue-400/30">
            <p className="text-sm text-blue-800 dark:text-blue-300">
              <strong>{commands.length}</strong> commands loaded
            </p>
            <p className="text-xs text-blue-600 dark:text-blue-400 mt-1">
              Technologies: {[...new Set(commands.map(c => c.technology))].join(', ')}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default WorkflowManager;