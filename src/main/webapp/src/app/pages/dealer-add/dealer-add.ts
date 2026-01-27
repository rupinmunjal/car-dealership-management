import { Component } from '@angular/core';
import { DealerService } from '../../services/dealer';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dealer-add',
  imports: [CommonModule, FormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule],
  templateUrl: './dealer-add.html',
  styleUrl: './dealer-add.css',
})
export class DealerAdd {

  dealer = { name: '', location: '' };
  message: string = '';
  error: string = '';

  constructor(
    private dealerService: DealerService,
    private router: Router
  ) {}

  onSubmit() {
    this.error = '';
    this.dealerService.addDealer(this.dealer).subscribe({
      next: () => {
        this.message = 'Dealer added successfully!';
        this.error = '';
        this.clearForm();
        setTimeout(() => this.router.navigate(['/dealers']), 1000);
      },
      error: () => {
        this.error = 'Failed to add dealer. Please try again.';
        this.message = '';
      }
    });
  }

  clearForm() {
    this.dealer = { name: '', location: '' };
  }

  resetForm() {
    this.clearForm();
    this.message = '';
    this.error = '';
  }
}
