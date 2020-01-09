import React, {useState, useEffect} from 'react'
import axios from 'axios'
import {Button, Panel, PageHeader} from 'react-bootstrap'
import AnswerTest from './AnswerTest'

export default function UserTests() {
  const [tests, setTests] = useState([]);
  const [showForm, setshowForm] = useState([])
  useEffect(() => {
    const fetchData = async () => {
      const url = 'https://owe6jjn5we.execute-api.us-east-1.amazonaws.com/dev/get-tests?user='+ localStorage.getItem('currentUsername') + '&role=candidate&status=assigned' ;
      const result = await axios(
        url,
      );
      setTests(result.data);
    };
    fetchData();
  }, []);
  return (
    <div>
      {tests.map((test, i)=>{
        return(
        <div> 
          <PageHeader>Moje testy do przejścia</PageHeader>
          <Panel>
            <Panel.Heading>
              <Panel.Title>{test.test_name}</Panel.Title>
            </Panel.Heading>
          <AnswerTest test={test} />
          </Panel>
        </div>
        )
    })}

    </div>
  )
}