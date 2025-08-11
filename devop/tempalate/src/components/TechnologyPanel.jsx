import React, { useState, useEffect } from 'react';
import { Search, Brain, Filter } from 'lucide-react';
import { defaultTechnologies } from '../data/defaultTechnologies';

const TechnologyPanel = ({
  selectedTechnologies,
  onTechnologySelect: onTechSelect,
  onTechnologyRemove,
  onTechForCheatSheetSelect,
  selectedTechForCheatSheet
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [availableTechnologies, setAvailableTechnologies] = useState(defaultTechnologies);

  // Get unique categories
  const categories = ['All', ...new Set(defaultTechnologies.map(tech => tech.category))];

  // Filter technologies based on search term and category
  const filteredTechnologies = availableTechnologies.filter(tech => {
    // Apply category filter
    if (categoryFilter !== 'All' && tech.category !== categoryFilter) {
      return false;
    }
    
    // Apply search term filter
    if (searchTerm.trim() !== '') {
      return tech.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tech.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tech.description?.toLowerCase().includes(searchTerm.toLowerCase());
    }
    
    return true;
  });

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSearch = () => {
    setIsSearching(true);
    // Simulate search functionality
    setTimeout(() => {
      const searchResults = defaultTechnologies.filter(tech =>
        tech.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        tech.description?.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setAvailableTechnologies([...defaultTechnologies, ...searchResults]);
      setIsSearching(false);
    }, 1000);
  };

  const handleAIDiscovery = () => {
    setIsSearching(true);
    // Simulate AI discovery of popular DevOps tools
    const aiDiscoveredTools = [
      {
        name: 'GitLab CI',
        category: 'CI/CD',
        description: 'GitLab Continuous Integration',
        baseCommand: 'gitlab-ci'
      },
      {
        name: 'CircleCI',
        category: 'CI/CD',
        description: 'Circle Continuous Integration',
        baseCommand: 'circleci'
      },
      {
        name: 'GitHub Actions',
        category: 'CI/CD',
        description: 'GitHub Actions CI/CD',
        baseCommand: 'gh'
      },
      {
        name: 'Azure CLI',
        category: 'Cloud',
        description: 'Microsoft Azure Command Line',
        baseCommand: 'az'
      },
      {
        name: 'GCP CLI',
        category: 'Cloud',
        description: 'Google Cloud Platform CLI',
        baseCommand: 'gcloud'
      },
      {
        name: 'Prometheus',
        category: 'Monitoring',
        description: 'Monitoring and alerting toolkit',
        baseCommand: 'prometheus'
      },
      {
        name: 'Grafana',
        category: 'Monitoring',
        description: 'Analytics and monitoring platform',
        baseCommand: 'grafana-cli'
      }
    ];

    setTimeout(() => {
      setAvailableTechnologies([...defaultTechnologies, ...aiDiscoveredTools]);
      setIsSearching(false);
    }, 1500);
  };

  const handleTechClick = (tech) => {
    // Directly show cheat sheet panel when clicking on a technology
    onTechForCheatSheetSelect(tech);
  };
  
  const handleCategoryChange = (category) => {
    setCategoryFilter(category);
  };

  return (
    <div className="bg-white dark:bg-dark-surface rounded-lg shadow-md p-0 h-full flex flex-col transition-colors duration-200 overflow-hidden">
      {/* Header */}
      <div className="p-4 border-b border-gray-200 dark:border-dark-border">
        <h2 className="text-xl font-semibold text-gray-800 dark:text-dark-text flex items-center">
          <span className="text-blue-500 mr-2">🧰</span> Technologies
        </h2>
        
        <div className="mt-3 relative">
          <input
            type="text"
            placeholder="Search technologies..."
            className="w-full p-2 pl-8 border border-gray-300 dark:border-dark-border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-dark-secondary dark:text-dark-text transition-colors duration-200"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Search className="absolute left-2.5 top-3 h-4 w-4 text-gray-400 dark:text-dark-muted" />
        </div>
      </div>
      
      {/* Category filters */}
      <div className="px-2 py-7 border-b border-gray-200 dark:border-dark-border bg-gray-50 dark:bg-dark-secondary/50 overflow-x-auto">
        <div className="flex space-x-2 pb-6">
          {categories.map((category) => (
            <button
              key={category}
              className={`px-3 py-2 text-xs rounded-full whitespace-nowrap transition-colors duration-200 ${categoryFilter === category
                ? 'bg-blue-500 text-white dark:bg-blue-600'
                : 'bg-gray-200 text-gray-700 dark:bg-dark-secondary dark:text-dark-muted hover:bg-gray-300 dark:hover:bg-dark-primary/30'}`}
              onClick={() => handleCategoryChange(category)}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      {/* AI Discover Button */}
      <div className="px-4 pt-3 pb-1">
        <button
          className="w-full flex items-center justify-center p-2 bg-gradient-to-r from-blue-500 to-purple-500 text-white rounded-md hover:from-blue-600 hover:to-purple-600 transition-colors duration-200 shadow-sm"
          onClick={handleAIDiscovery}
        >
          <Brain className="h-4 w-4 mr-2" />
          AI Discover
        </button>
      </div>

      {/* Available Technologies */}
      <div className="px-4 pt-2 pb-4 flex-grow overflow-hidden flex flex-col">
        <h3 className="text-sm font-medium mb-2 text-gray-700 dark:text-dark-muted flex items-center">
          <Filter className="h-3 w-3 mr-1" /> Technologies
          {filteredTechnologies.length > 0 && (
            <span className="ml-1 text-xs text-gray-500 dark:text-dark-muted">({filteredTechnologies.length})</span>
          )}
        </h3>
        
        {isSearching ? (
          <div className="text-center py-4 flex-grow">
            <p className="text-gray-500 dark:text-dark-muted">Searching...</p>
          </div>
        ) : filteredTechnologies.length === 0 ? (
          <div className="text-center py-4 flex-grow">
            <p className="text-gray-500 dark:text-dark-muted">No technologies found</p>
            <button 
              className="mt-2 text-blue-500 text-sm hover:underline"
              onClick={() => {
                setSearchTerm('');
                setCategoryFilter('All');
              }}
            >
              Clear filters
            </button>
          </div>
        ) : (
          <div className="overflow-y-auto flex-grow">
            <div className="grid grid-cols-1 gap-2 pr-1">
              {filteredTechnologies.map((tech) => (
                <div
                  key={tech.name}
                  className={`p-3 rounded-md cursor-pointer transition-all duration-200 ${selectedTechForCheatSheet?.name === tech.name
                    ? 'bg-blue-50 dark:bg-dark-primary/20 border-l-4 border-blue-500 dark:border-blue-400 shadow-sm'
                    : 'hover:bg-gray-50 dark:hover:bg-dark-secondary hover:shadow-sm border border-transparent hover:border-gray-200 dark:hover:border-dark-border'}`}
                  onClick={() => handleTechClick(tech)}
                >
                  <div className="flex items-center">
                    <span className="text-xl mr-3 bg-gray-100 dark:bg-dark-secondary h-8 w-8 flex items-center justify-center rounded-md">{tech.icon}</span>
                    <div className="flex-grow">
                      <div className="font-medium text-gray-800 dark:text-dark-text">{tech.name}</div>
                      <div className="text-xs text-gray-500 dark:text-dark-muted flex items-center">
                        <span className="bg-gray-200 dark:bg-dark-secondary px-1.5 py-0.5 rounded text-xs mr-1">{tech.category}</span>
                        {tech.baseCommand && (
                          <span className="font-mono text-xs text-gray-500 dark:text-dark-muted">{tech.baseCommand}</span>
                        )}
                      </div>
                    </div>
                    <div className="ml-auto text-blue-500 dark:text-blue-400 text-sm font-medium">
                      View Cheat Sheet →
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default TechnologyPanel;