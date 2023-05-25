import React from 'react';
import { BrowserRouter, Route } from 'react-router-dom';

import SentimentAnalysis from './SentimentAnalysis';

const AdminRoutes = () => {
  return (
    <BrowserRouter>
      <Route path="/" exact>
        <SentimentAnalysis />
      </Route>
    </BrowserRouter>
  );
};

export default AdminRoutes;
