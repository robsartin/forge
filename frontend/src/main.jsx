import React from 'react'
import ReactDOM from 'react-dom/client'
import GraphEditor from './components/GraphEditor'
import './styles/graph-editor.css'
import './styles/graph-visualization.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <GraphEditor />
  </React.StrictMode>
)
