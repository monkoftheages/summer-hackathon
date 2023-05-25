/* eslint-disable jsx-a11y/click-events-have-key-events */
/* eslint-disable jsx-a11y/no-noninteractive-element-interactions */
import React from 'react';

import FrankDogLogo from './frankDogLogo.svg';
import PageContainer from './shared-components/PageContainer/PageContainer';

import styles from './SentimentAnalysis.module.css';

const JOB_URL = 'http://localhost:8080/hackathon/job';
const QUESTIONS_URL = 'http://localhost:8080/hackathon/questions';

const SentimentAnalysis = () => {
  const [inputValue, setInputValue] = React.useState();
  const [questions, setQuestions] = React.useState();
  const [isOpen, setOpen] = React.useState(false);
  const [selectedQuestion, setSelectedQuestion] = React.useState();
  const [info, setInfo] = React.useState();

  const onSend = React.useCallback(async () => {
    try {
      const response = await fetch(JOB_URL, {
        method: 'POST',
        body: inputValue,
        mode: 'cors',
      });
    } catch (error) {
      console.error(error);
    }
  }, [inputValue]);

  React.useEffect(() => {
    if (selectedQuestion) {
      fetch(`${QUESTIONS_URL}/${selectedQuestion.id}`, {
        method: 'GET',
        mode: 'cors',
        headers: new Headers({
          Accept: 'application/json',
        }),
      })
        .then((response) => response.json())
        .then((data) => setInfo(data));
    }
  }, [selectedQuestion]);

  React.useEffect(() => {
    fetch(QUESTIONS_URL, {
      method: 'GET',
      mode: 'cors',
      headers: new Headers({
        Accept: 'application/json',
      }),
    })
      .then((response) => response.json())
      .then((data) => setQuestions(data.questions));
  }, []);

  return (
    <PageContainer>
      <div className={styles.container}>
        <>
          <div className={styles.header}>
            <div className={styles.headerLeft}>
              <div className={styles.logo}>
                <img src={FrankDogLogo} alt="Frank Dog Logo" />
              </div>
              <h1>FFF PSA-Product Sentiment Analysis</h1>
            </div>
          </div>
          <div className={styles.productView}>
            <h3>Ask your question about a user</h3>
            <div>
              <input
                placeholder="write your question here"
                id="user-question"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
              />
              <button type="submit" onClick={onSend}>
                SEND
              </button>
            </div>
          </div>
          {/* <ProgressBar
            completed={60}
            className={styles.progressBar}
            height={30}
          /> */}
          <h3>Questions history</h3>
          <div
            className={styles.dropdownButtonContainer}
            id="section1-sr-label-container"
          >
            <button
              id="section1-sr-label-button"
              className={styles.select}
              type="button"
              onClick={() => setOpen(!isOpen)}
            >
              <span>{selectedQuestion?.query ?? 'Select One'}</span>
            </button>
          </div>
          {isOpen && (
            <div className={styles.questionsDropdown}>
              {questions?.map((question) => (
                <p
                  key={question.id}
                  onClick={() => {
                    setSelectedQuestion(question);
                    setOpen(false);
                  }}
                >
                  {question.query}
                </p>
              ))}
            </div>
          )}
          {info && !isOpen && (
            <div className={styles.questionInfo}>
              <p>{`The average sentiment is ${info.averageSentiment}%.`}</p>
              <p>{`The percentage of users that have a sentiment rating over 50% is ${info.percentageHighSentiment}%.`}</p>
              {info.highSentimentUsers && info.highSentimentUsers.length > 0 && (
                <table>
                  <thead>
                    <tr>
                      <td>User ID</td>
                    </tr>
                  </thead>
                  <tbody>
                    {info.highSentimentUsers.map((user) => (
                      <tr key={user}>
                        <td>{user}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}
        </>
      </div>
    </PageContainer>
  );
};

export default SentimentAnalysis;
