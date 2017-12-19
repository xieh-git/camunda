import React from 'react';
import { mount } from 'enzyme';

import Icon from './Icon';


it('should render without crashing', () => {
  mount(<Icon />);
});

it('should render a tag as provided as a property', () => {
  const node = mount(<Icon tag='span' />);
  
  expect(node.find('.Icon')).toHaveTagName('span');
});

it('should render an inline SVG', () => {
  const node = mount(<Icon />);
  
  expect(node.find('svg')).toBePresent();
});

it('should render a fill attribute according to fill provided as a prop', () => {
  const node = mount(<Icon fill='red'/>);
  
  expect(node.find('svg')).toMatchSelector('[fill="red"]');
})

it('should render an element with a class when "backgroundImg" was provided as a property', () => {
  const node = mount(<Icon backgroundImg src='plus'/>);
  
  expect(node.find('.Icon')).toMatchSelector('.Icon--plus');
});