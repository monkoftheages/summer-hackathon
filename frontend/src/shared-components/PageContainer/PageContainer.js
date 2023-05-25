import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';

import styles from './PageContainer.module.css';

const PageContainer = ({ className, children }) => (
  <div className={classNames(styles.container, className)}>{children}</div>
);

PageContainer.propTypes = {
  className: PropTypes.string,
  children: PropTypes.node.isRequired,
};

PageContainer.defaultProps = {
  className: null,
};

export default PageContainer;
