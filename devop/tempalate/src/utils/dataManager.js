// src/utils/dataManager.js
const STORAGE_KEYS = {
  TECHNOLOGIES: 'devops_selected_technologies',
  WORKFLOW_HISTORY: 'devops_workflow_history',
  CHEAT_SHEETS: 'devops_cheat_sheets'
};

export const loadTechnologies = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEYS.TECHNOLOGIES);
    return stored ? JSON.parse(stored) : [];
  } catch (error) {
    console.error('Error loading technologies:', error);
    return [];
  }
};

export const saveTechnologies = (technologies) => {
  try {
    localStorage.setItem(STORAGE_KEYS.TECHNOLOGIES, JSON.stringify(technologies));
  } catch (error) {
    console.error('Error saving technologies:', error);
  }
};

export const loadWorkflowHistory = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEYS.WORKFLOW_HISTORY);
    return stored ? JSON.parse(stored) : [];
  } catch (error) {
    console.error('Error loading workflow history:', error);
    return [];
  }
};

export const saveWorkflowHistory = (history) => {
  try {
    localStorage.setItem(STORAGE_KEYS.WORKFLOW_HISTORY, JSON.stringify(history));
  } catch (error) {
    console.error('Error saving workflow history:', error);
  }
};

export const loadCheatSheets = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEYS.CHEAT_SHEETS);
    return stored ? JSON.parse(stored) : {};
  } catch (error) {
    console.error('Error loading cheat sheets:', error);
    return {};
  }
};

export const saveCheatSheets = (cheatSheets) => {
  try {
    localStorage.setItem(STORAGE_KEYS.CHEAT_SHEETS, JSON.stringify(cheatSheets));
  } catch (error) {
    console.error('Error saving cheat sheets:', error);
  }
};

export const exportData = () => {
  const data = {
    technologies: loadTechnologies(),
    workflowHistory: loadWorkflowHistory(),
    cheatSheets: loadCheatSheets(),
    exportedAt: new Date().toISOString()
  };
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'devops-build-flow-backup.json';
  a.click();
  URL.revokeObjectURL(url);
};

export const importData = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target.result);
        
        if (data.technologies) {
          saveTechnologies(data.technologies);
        }
        if (data.workflowHistory) {
          saveWorkflowHistory(data.workflowHistory);
        }
        if (data.cheatSheets) {
          saveCheatSheets(data.cheatSheets);
        }
        
        resolve(data);
      } catch (error) {
        reject(new Error('Invalid file format'));
      }
    };
    reader.onerror = () => reject(new Error('File reading failed'));
    reader.readAsText(file);
  });
};

export const clearAllData = () => {
  if (window.confirm('Are you sure you want to clear all data? This action cannot be undone.')) {
    localStorage.removeItem(STORAGE_KEYS.TECHNOLOGIES);
    localStorage.removeItem(STORAGE_KEYS.WORKFLOW_HISTORY);
    localStorage.removeItem(STORAGE_KEYS.CHEAT_SHEETS);
    window.location.reload();
  }
};