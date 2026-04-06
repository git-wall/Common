import React, { useState, useRef, useEffect } from 'react';
import { useDrag } from 'react-dnd';
import { getCheatSheet } from '../data/cheatSheets';

const DraggableCheatSheetItem = ({ item, techName }) => {
  const [{ isDragging }, drag] = useDrag(() => ({
    type: 'cheatsheet-item',
    item: { ...item, techName },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  }));

  return (
    <div
      ref={drag}
      className={`p-3 border border-gray-200 dark:border-dark-border rounded-md cursor-move hover:bg-gray-50 dark:hover:bg-dark-secondary transition-colors duration-200 ${
        isDragging ? 'opacity-50' : ''
      }`}
    >
      <div className="font-medium text-sm text-gray-800 dark:text-dark-text">{item.key}</div>
      <div className="text-xs text-gray-600 dark:text-dark-muted mt-1">{item.description}</div>
      {item.example && (
        <div className="text-xs text-blue-600 dark:text-blue-400 mt-1 font-mono">{item.example}</div>
      )}
    </div>
  );
};

const CheatSheetPanel = ({ selectedTech, onClose }) => {
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const panelRef = useRef(null);

  // Initialize position when panel opens
  useEffect(() => {
    if (selectedTech && panelRef.current) {
      // Position the panel on the right side by default, but allow it to be moved
      const rect = panelRef.current.getBoundingClientRect();
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      
      setPosition({
        x: Math.min(windowWidth - 350, windowWidth * 0.7), // Start from right side
        y: Math.max(100, (windowHeight - rect.height) / 4) // Start from top quarter
      });
    }
  }, [selectedTech]);

  const handleMouseDown = (e) => {
    // Only start dragging if clicking on the header area
    if (e.target.closest('.drag-handle')) {
      setIsDragging(true);
      const rect = panelRef.current.getBoundingClientRect();
      setDragOffset({
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      });
      e.preventDefault();
    }
  };

  const handleMouseMove = (e) => {
    if (isDragging) {
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const panelWidth = 320; // Approximate panel width
      const panelHeight = 500; // Approximate panel height
      
      // Calculate new position with bounds checking
      let newX = e.clientX - dragOffset.x;
      let newY = e.clientY - dragOffset.y;
      
      // Keep panel within screen bounds
      newX = Math.max(0, Math.min(newX, windowWidth - panelWidth));
      newY = Math.max(0, Math.min(newY, windowHeight - panelHeight));
      
      setPosition({ x: newX, y: newY });
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // Add global mouse event listeners
  useEffect(() => {
    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      
      return () => {
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isDragging, dragOffset]);

  if (!selectedTech) {
    return null;
  }

  const cheatSheet = getCheatSheet(selectedTech.name);

  return (
    <div
      ref={panelRef}
      className={`fixed bg-white dark:bg-dark-surface rounded-lg shadow-xl border border-gray-200 dark:border-dark-border p-0 w-80 max-h-[600px] z-50 transition-shadow duration-200 ${
        isDragging ? 'shadow-2xl cursor-grabbing' : 'shadow-lg'
      }`}
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        userSelect: isDragging ? 'none' : 'auto'
      }}
    >
      {/* Draggable Header */}
      <div
        className="drag-handle flex items-center justify-between p-4 border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-secondary/50 rounded-t-lg cursor-grab active:cursor-grabbing"
        onMouseDown={handleMouseDown}
      >
        <div className="flex items-center">
          <div className="flex space-x-1 mr-3">
            <div className="w-3 h-3 bg-red-400 rounded-full"></div>
            <div className="w-3 h-3 bg-yellow-400 rounded-full"></div>
            <div className="w-3 h-3 bg-green-400 rounded-full"></div>
          </div>
          <h2 className="text-lg font-semibold text-gray-800 dark:text-dark-text">
            Cheat Sheet
          </h2>
        </div>
        <button
          onClick={onClose}
          className="text-gray-500 hover:text-gray-700 dark:text-dark-muted dark:hover:text-dark-text text-xl hover:bg-gray-200 dark:hover:bg-dark-primary/20 w-6 h-6 rounded-full flex items-center justify-center transition-colors duration-200"
          aria-label="Close Cheat Sheet"
        >
          ×
        </button>
      </div>

      {/* Content */}
      <div className="p-4">
        <div className="mb-4">
          <div className="flex items-center mb-2">
            <span className="text-xl mr-2">{selectedTech.icon}</span>
            <h3 className="font-medium text-lg text-gray-700 dark:text-dark-text">{selectedTech.name}</h3>
          </div>
          <p className="text-sm text-gray-600 dark:text-dark-muted">{selectedTech.description}</p>
          {selectedTech.baseCommand && (
            <div className="mt-2 text-xs">
              <span className="text-gray-500 dark:text-dark-muted">Base command: </span>
              <code className="bg-gray-100 dark:bg-dark-secondary px-2 py-1 rounded text-blue-600 dark:text-blue-400 font-mono">
                {selectedTech.baseCommand}
              </code>
            </div>
          )}
        </div>

        {cheatSheet && cheatSheet.length > 0 ? (
          <div className="max-h-96 overflow-y-auto">
            <p className="text-sm text-gray-600 dark:text-dark-muted mb-3 flex items-center">
              <span className="mr-1">💡</span>
              Drag items below to the form to build your command
            </p>
            <div className="space-y-3">
              {cheatSheet.map((item, index) => (
                <DraggableCheatSheetItem
                  key={index}
                  item={item}
                  techName={selectedTech.name}
                />
              ))}
            </div>
          </div>
        ) : (
          <div className="text-center text-gray-500 dark:text-dark-muted py-8">
            <div className="text-4xl mb-2">📋</div>
            <p>No cheat sheet available for {selectedTech.name}</p>
            <p className="text-sm mt-2">Basic commands and parameters will be generated automatically</p>
          </div>
        )}
      </div>

      {/* Resize indicator */}
      <div className="absolute bottom-0 right-0 w-4 h-4 text-gray-400 dark:text-dark-muted opacity-50">
        <svg viewBox="0 0 16 16" className="w-full h-full">
          <path d="M16 0v16H0L16 0z" fill="currentColor" opacity="0.1"/>
          <path d="M6 16L16 6v4L10 16H6zM11 16L16 11v3L14 16h-3z" fill="currentColor"/>
        </svg>
      </div>
    </div>
  );
};

export default CheatSheetPanel;