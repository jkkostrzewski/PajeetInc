import { LinkContainer } from 'react-router-bootstrap';
import React, { Component, Fragment } from 'react';
import { Link, withRouter } from 'react-router-dom';

import { Nav, Navbar, NavItem } from 'react-bootstrap';
import Routes from './Routes';
import { Auth } from 'aws-amplify';
import { IntlProviderWrapper } from './translations/IntProviderWrapper'
import { FormattedMessage } from 'react-intl';
import { LanguageSwitch } from './components/LanguageSwitch'
import './App.css';


class App extends Component {
	constructor(props) {
		super(props);

		this.state = {
			isAuthenticated: false,
			isAuthenticating: true
		};
	}

	async componentDidMount() {
		try {
			if (await Auth.currentSession()) {
				this.userHasAuthenticated(true);
			}
		} catch (e) {
			if (e !== 'No current user') {
				alert(e);
			}
		}

		this.setState({ isAuthenticating: false });
	}

	userHasAuthenticated = authenticated => {
		this.setState({ isAuthenticated: authenticated });
	};

	handleLogout = async event => {
		await Auth.signOut();

		this.userHasAuthenticated(false);
		this.props.history.push('/login');
	};
	render() {
		const childProps = {
			isAuthenticated: this.state.isAuthenticated,
			userHasAuthenticated: this.userHasAuthenticated
		};
		return (
			<IntlProviderWrapper>
				<div className="App container">
					<Navbar fluid collapseOnSelect>
						<Navbar.Header>
							<Navbar.Brand>
								<Link to="/">Test application</Link>
							</Navbar.Brand>
							<Navbar.Toggle />
						</Navbar.Header>
						<Navbar.Collapse>
							<Nav>
							<NavItem>
								<LanguageSwitch />
							</NavItem>
							</Nav>
							<Nav pullRight>
								{this.state.isAuthenticated ? (
									<NavItem onClick={this.handleLogout}>Logout</NavItem>
								) : (
									<Fragment>
										<LinkContainer to="/signup">
											<NavItem><FormattedMessage id="user.signUp"/></NavItem>
										</LinkContainer>
										<LinkContainer to="/login">
											<NavItem><FormattedMessage id="user.signIn"/></NavItem>
										</LinkContainer>
									</Fragment>
								)}
							</Nav>
						</Navbar.Collapse>
					</Navbar>
					<Routes childProps={childProps} />
				</div>
			</IntlProviderWrapper>
		
		);
	}
}

export default withRouter(App);